MG.Name = "The Building Game"
MG.Description = "A game of telephone, but with builds. Original by SethBling"
MG.MinPlayers = 3
MG.MaxPlayers = 8
MG.Schematic = "tbg"
MG.GameMode = GM_ADVENTURE
MG.Icon = "CRAFTING_TABLE"

MG.Slots = {}
MG.Round = 1
MG.NumRounds = 1
MG.RoundStage = 1
MG.SlotOffset = 0

MG.Prompting = false
MG.Prompts = {}
MG.HistoricPrompts = {}

MG.Building = false
MG.BuildReady = {}

MG.CurBar = nil
MG.TaskEnd = 0

MG.IsEnding = false
MG.IsShowcasing = false
MG.ShowcaseSlot = 1

MG.DefaultPrompts = { "No prompt found! Just make something up :)", "404 No Prompt Found", "Something", "Whatever you want", "I couldn't make a prompt in time!", "Herobrine" }

function MG:Start()
    local origin = self:GetMapOrigin()
    local world = origin:GetWorld()
    local x = origin:GetX()
    local y = origin:GetY()
    local z = origin:GetZ()
    local players = self:GetPlayers()
    local count = #players
    self.NumRounds = count
    self.Slots = {}
    local head = 1
    for _,ply in ipairs(players) do
        self.Slots[head] = ply:GetUUID()
        head = head + 1
    end
    local bb = BossBar("Timer")
    bb:SetPlayers(self:GetPlayers())
    local ct = 0
    self:Broadcast("<gold>Game starts in 10 seconds</gold>")
    self:BroadcastSound("BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING", "NOTE_PLING")
    for q=2,count,1 do
        for v=2,count,1 do
            timer.Simple((ct / 10) + 2, function()
                local cx = x + ((q - 1) * 44)
                local cz = z + ((v - 1) * 44)
                local loc = Location(world, cx, y, cz)
                self:PasteSchematic("tbg", loc)
            end )
            ct = ct + 1
        end
    end
    for i=1,100,1 do
        timer.Simple(i / 10, function()
            bb:SetProgress(1 - (i / 100))
        end)
    end
    timer.Simple(10, function()
        self:BroadcastSound("BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING", "NOTE_PLING")
        bb:SetPlayers({})
        self:RoundStep()
    end )
end

-- BUILD AREA:
-- 3, 0, 3 to 40, 13, 40
-- 7, 14, 7 to 36, 45, 36
function MG:InBounds(index, loc)
    local origin = self:GetMapOrigin()
    local ox = origin:GetX() + ((index - 1) * 44)
    local oy = origin:GetY()
    local oz = origin:GetZ() + ((self.Round - 1) * 44)
    local lx = loc:GetX()
    local ly = loc:GetY()
    local lz = loc:GetZ()
    if (ly < (oy - 0.1)) then return false end
    if (ly < (oy + 14)) then
        if (lx < (ox + 3)) then return false end
        if (lx > (ox + 39.9)) then return false end
        if (lz < (oz + 3)) then return false end
        if (lz > (oz + 39.9)) then return false end
    else
        if (lx < (ox + 7)) then return false end
        if (lx > (ox + 34.9)) then return false end
        if (lz < (oz + 7)) then return false end
        if (lz > (oz + 34.9)) then return false end
        if (ly > (oy + 44)) then return false end
    end
    return true
end

function MG:OnMove(ply, from, to)
    if (ply:GetGameMode() ~= GM_SPECTATOR) then return false end
    local idx
    if (self.IsEnding and self.IsShowcasing) then
        idx = self.ShowcaseSlot
    else
        idx = self:GetPlayerIndex(ply)
    end
    if (self:InBounds(idx, to)) then return end
    if (not self:InBounds(idx, from)) then
        ply:TeleportAsync(self:GetSpawn(ply))
    end
    return true
end

function MG:OnPlace(ply, block)
    if (self.IsEnding) then return true end
    if (ply:GetGameMode() ~= GM_CREATIVE) then return true end
    return (not self:InBounds(self:GetPlayerIndex(ply), block:GetLocation()))
end

function MG:OnBreak(ply, block)
    if (self.IsEnding) then return true end
    if (ply:GetGameMode() ~= GM_CREATIVE) then return true end
    return (not self:InBounds(self:GetPlayerIndex(ply), block:GetLocation()))
end

function MG:OnChat(ply, msg)
    if (self.Prompting) then
        local idx = self:GetPlayerIndex(ply)
        if (self.Prompts[idx] ~= nil) then
            ply:SendMessage("<gold>* Updated your submission to: <white>" .. msg .. "</white></gold>")
            self.Prompts[self:GetPlayerIndex(ply)] = msg
        else
            ply:SendMessage("<gold>* Submitted prompt: <white>" .. msg .. "</white></gold>")
            self.Prompts[self:GetPlayerIndex(ply)] = msg
            local all = true
            for _,v in ipairs(self:GetPlayers()) do
                if (self.Prompts[self:GetPlayerIndex(v)] == nil) then
                    all = false
                    break
                end
            end
            if (all) then
                timer.Simple(0, function()
                    if (not self.Prompting) then return end
                    self.Prompting = false
                    self:RoundStep()
                end )
            end
        end
        return true
    elseif (self.Building) then
        if (string.lower(msg) == "!done") then
            local uuid = ply:GetUUID()
            local cur = self.BuildReady[uuid]
            if (cur) then
                self.BuildReady[uuid] = false
                ply:SendMessage("<red>* Not done</red>")
            else
                self.BuildReady[uuid] = true
                ply:SendMessage("<green>* Done! Use <white>!done</white> again to set your build as <red>Not done</red></green>")
                local all = true
                for _,p in ipairs(self:GetPlayers()) do
                    if (not (self.BuildReady[p:GetUUID()] or false)) then
                        all = false
                        break
                    end
                end
                if (all) then
                    timer.Simple(0, function()
                        if (not self.Building) then return end
                        self.Prompting = false
                        self:RoundStep()
                    end )
                end
            end
            return true
        end
    end
end

function MG:RoundStep()
    if (self.RoundStage ~= 1) then self:BroadcastSound("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP") end
    if (self.CurBar ~= nil) then self.CurBar:SetPlayers({}) end
    if (self.RoundStage == 1 or self.RoundStage == 3) then
        local guess = false
        if (self.RoundStage == 3) then
            self.SlotOffset = self.SlotOffset + 1
            self.RoundStage = 4
            guess = true
        else
            self.RoundStage = 2
        end
        self.Prompts = {}
        self.TaskEnd = os.time() + 120
        local bb = BossBar("Timer", BC_GREEN, BS_SOLID)
        local players = self:GetPlayers()
        bb:SetPlayers(players)
        self.CurBar = bb
        self.Prompting = true
        local msg = "<gold>* Enter a prompt for people to build</gold>"
        if (guess) then
            msg = "<gold>* Enter a guess for the build in chat</gold>"
        end
        for _,ply in ipairs(players) do
            ply:TeleportAsync(self:GetSpawn(ply))
            ply:SetGameMode(GM_SPECTATOR)
            ply:SendMessage(msg)
        end
    elseif (self.RoundStage == 2 or self.RoundStage == 4) then
        self.Prompting = false
        if (self.RoundStage == 4) then
            if (self.Round >= self.NumRounds) then
                local players = self:GetPlayers()
                local pmts = {}
                for _,ply in ipairs(players) do
                    local idx = self:GetPlayerIndex(ply)
                    local prompt = self.Prompts[idx]
                    if (prompt == nil) then
                        prompt = self.DefaultPrompts[math.random(#self.DefaultPrompts)]
                    end
                    pmts[idx] = prompt
                end
                self.HistoricPrompts[self.NumRounds + 1] = pmts
                self:FinalRound()
                return
            else
                self.Round = self.Round + 1
            end
        end
        self.RoundStage = 3
        self.SlotOffset = self.SlotOffset + 1
        self.TaskEnd = os.time() + 300
        local bb = BossBar("Timer", BC_GREEN, BS_SOLID)
        local players = self:GetPlayers()
        bb:SetPlayers(players)
        self.CurBar = bb
        self.Building = true
        local pmts = {}
        for _,ply in ipairs(players) do
            ply:SetGameMode(GM_CREATIVE)
            ply:TeleportAsync(self:GetSpawn(ply))
            local idx = self:GetPlayerIndex(ply)
            local prompt = self.Prompts[idx]
            if (prompt == nil) then
                prompt = self.DefaultPrompts[math.random(#self.DefaultPrompts)]
            end
            pmts[idx] = prompt
            ply:SendMessage("<gold>* Build your prompt! When you are done, type <white>!done</white> into chat!</gold>")
            ply:SendMessage("<gold>* Your prompt is: <white>" .. prompt .. "</white></gold>")
        end
        self.HistoricPrompts[self.Round] = pmts
    end
end

function MG:FinalRound()
    if (self.CurBar ~= nil) then self.CurBar:SetPlayers({}) end
    self.IsEnding = true
    local ct = self.NumRounds
    local players = self:GetPlayers()
    for _,ply in ipairs(players) do
        ply:SendMessage("<green>* Game over! Showcasing builds in 10 seconds...</green>")
        ply:SetGameMode(GM_SPECTATOR)
    end
    self:BroadcastSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP")
    local delay = 0
    for x=1,ct,1 do
        local msg = ""
        local pmts = self.HistoricPrompts[x]
        for z=1,ct,1 do
            local fx = x
            local fz = z
            delay = delay + 10
            timer.Simple(delay, function()
                self.IsShowcasing = true
                self.ShowcaseSlot = fx
                self.Round = fz
                for _,ply in ipairs(players) do
                    ply:TeleportAsync(self:GetSpawnAtIndex(fx, fz))
                end
                self:Broadcast("<gold>= Round " .. fz .. " =</gold>")
                self:BroadcastSound("BLOCK_WOODEN_PRESSURE_PLATE_CLICK_ON", "BLOCK_WOOD_PRESSUREPLATE_CLICK_ON", "WOOD_CLICK")
                if (fz > 1) then
                    msg = (msg .. " <white>-></white> ")
                end
                local pmt = pmts[fz]
                if (pmt == nil) then
                    msg = (msg .. "<gray>No Prompt</gray>")
                    self:Broadcast("<gray>The player assigned to this build left the game</gray>")
                else
                    msg = (msg .. "<gold>" .. pmt .. "</gold>")
                    self:Broadcast(msg)
                end
                if (fz >= ct) then
                    local finalPmt = pmt[fz + 1]
                    if (finalPmt ~= nil) then
                        timer.Simple(2, function()
                            self:Broadcast("<gray>Final Guess</gray><white>: " .. finalPmt .. "</white>")
                            self:BroadcastSound("ENTITY_CREEPER_DEATH", "CREEPER_DEATH")
                        end )
                    end
                end
            end )
        end
    end
    delay = delay + 10
    timer.Simple(delay, function()
        self:End()
    end )
end

function MG:Tick()
    if (self.Prompting or self.Building) then
        local dur = 120
        if (self.Building) then dur = 300 end
        local pc = math.min(os.difftime(self.TaskEnd, os.time()) / dur, 1)
        if (pc < 0) then
            self.Prompting = false
            self:RoundStep()
        else
            self.CurBar:SetProgress(pc)
        end
    end
end

function MG:Stop()
    if (self.CurBar ~= nil) then self.CurBar:SetPlayers({}) end
end

function MG:Leave(_)
end

function MG:OnDamage(_, _, _)
    return true
end

function MG:GetMapOrigin()
    local chunk = self:GetRegion():GetMinChunk()
    local block = chunk:GetBlock(0, 40, 0)
    local loc = block:GetLocation()
    return loc
end

function MG:GetPlayerIndex(ply)
    local index = 1
    local target = ply:GetUUID()
    for idx,uuid in pairs(self.Slots) do
        if (uuid == target) then
            index = idx
            break
        end
    end
    index = index + self.SlotOffset
    if (index > self.NumRounds) then index = 1 end
    return index
end

function MG:GetSpawn(ply)
    local index = self:GetPlayerIndex(ply)
    return self:GetSpawnAtIndex(index, self.Round)
end

function MG:GetSpawnAtIndex(x, z)
    local origin = self:GetMapOrigin()
    origin:SetX(origin:GetX() + 21 + ((x - 1) * 44))
    origin:SetZ(origin:GetZ() + 14 + ((z - 1) * 44))
    origin:SetYaw(0)
    origin:SetPitch(-17.5)
    return origin
end

function MG:GetUniqueTag()
    local region = self:GetRegion()
    local centerBlock = region:GetCenterBlock()
    return "tbg_" .. centerBlock:GetX() .. "_" .. centerBlock:GetZ()
end

function MG:GetSchematicPos()
    return self:GetMapOrigin()
end
