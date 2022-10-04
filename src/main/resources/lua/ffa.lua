MG.Name = "Free for All"
MG.Description = "Last man standing wins"
MG.MinPlayers = 2
MG.MaxPlayers = 16
MG.Schematic = "lab"
MG.GameMode = GM_ADVENTURE
MG.Icon = "SKELETON_SKULL"

MG.Deaths = {}
MG.StartTime = 0
MG.Started = false

function MG:Start()
    self:Broadcast("<red>* </red><gradient:dark_red:red:dark_red>Game starts in <gold>30</gold> seconds</gradient>")
    timer.Simple(15, function()
        self:Broadcast("<red>* </red><gradient:dark_red:red:dark_red>Game starts in <gold>15</gold> seconds</gradient>")
    end)
    for i=0,5,1 do
        timer.Simple(25 + i, function()
            if (i == 5) then
                self.StartTime = os.time()
                self.Started = true
                for _,v in pairs(self:GetPlayers()) do
                    local inv = v:GetInventory()
                    inv:SetItem(0, ItemStack(Material("IRON_SWORD"), 1))
                    inv:SetItem(1, ItemStack(Material("BOW"), 1))
                    inv:SetItem(2, ItemStack(Material("COOKED_BEEF"), 64))
                    inv:SetItem(3, ItemStack(Material("ARROW"), 64))
                    inv:SetArmorContents({
                        ItemStack(Material("LEATHER_BOOTS"), 1),
                        ItemStack(Material("LEATHER_LEGGINGS"), 1),
                        ItemStack(Material("IRON_CHESTPLATE"), 1),
                        nil
                    })
                    if (inv:SupportsOffhand()) then inv:SetItemOffhand(ItemStack(Material("SHIELD"), 1)) end
                    v:SetGameMode(GM_SURVIVAL)
                    v:SendMessage("<gradient:#00ff00:#00aa00>* GO! GO! GO!</gradient>")
                end
                self:BroadcastSound("ENTITY_ENDER_DRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL", "ENDERDRAGON_GROWL")
            else
                for _,v in pairs(self:GetPlayers()) do
                    v:ShowTitle("<bold><gold>" .. (5 - i) .. "</gold></bold>", "")
                end
                self:BroadcastSound("BLOCK_BELL_USE", "BLOCK_NOTE_BLOCK_BASS", "BLOCK_NOTE_BASS", "NOTE_BASS")
            end
        end )
    end
end

MG.SuddenDeath = false
function MG:Tick()
    if (not self.Started) then return end
    if (self.SuddenDeath) then
        local airMat = Material("AIR")
        local lavaMat = Material("LAVA")
        for _,ply in pairs(self:GetPlayers()) do
            if (ply:GetGameMode() == GM_SPECTATOR) then
                goto continue
            end
            if (math.random() <= 0.3) then
                local blocks = {}
                local pos = ply:GetPos()
                local world = pos:GetWorld()
                local x = pos:GetBlockX()
                local y = pos:GetBlockY()
                local z = pos:GetBlockZ()
                for mx=-4,4,1 do
                    for my=-4,4,1 do
                        for mz=-4,4,1 do
                            local block = Location(world, x + mx, y + my, z + mz):GetBlock()
                            if (string.lower(block:GetMaterial():GetName()) ~= "air") then
                                table.insert(blocks, block)
                            end
                        end
                    end
                end
                if ((#blocks) < 1) then goto continue end
                local idx = math.random(#blocks)
                local block = blocks[idx]
                local setMat = airMat
                if (math.random() <= 0.25) then
                    setMat = lavaMat
                end
                block:SetMaterial(setMat)
            end
            ::continue::
        end
    else
        local now = os.time()
        local elapsed = math.abs(os.difftime(self.StartTime, now))
        if (elapsed > 300) then
            for _,ply in pairs(self:GetPlayers()) do
                ply:ShowTitle("<dark_red>SUDDEN</dark_red>", "<red>DEATH</red>")
                ply:SendMessage("<red>* Nobody has won within 5 minutes, so sudden death has begun!</red>")
            end
            self:BroadcastSound("ENTITY_ENDER_DRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL", "ENDERDRAGON_GROWL")
            self.SuddenDeath = true
        end
    end
end

function MG:GetSpawn(_)
    return self:GetRegion():GetCenterBlock():GetLocation()
end

function MG:GetSchematicPos()
    return self:GetRegion():GetCenterBlock():GetLocation()
end

function MG:OnDamage()
    if (not self.Started) then return true end
    return false
end

function MG:RecordDeath(uuid)
    local idx = 1
    local size = (#self.Deaths)
    while (idx <= size) do
        if (self.Deaths[idx] == uuid) then return end
        idx = idx + 1
    end
    self.Deaths[idx] = uuid
end

function MG:OnDeath(ply)
    if (not self.Started) then return true end
    local players = self:GetPlayers()
    local place = ((#players) - (#self.Deaths))
    local suffix = "rd"
    local thenEnd = false
    self:RecordDeath(self.Deaths, ply:GetUUID())
    if (place == 2) then
        suffix = "nd"
        thenEnd = true
    elseif (place == 1) then
        timer.Simple(0, function()
            self:EndRoutine()
        end )
        return false
    end
    ply:ShowTitle("<red>You died!</red>", "<dark_red>Finished in " .. place .. suffix .. " place</dark_red>")
    timer.Simple(0, function()
        self:BroadcastSound("ENTITY_CREEPER_DEATH", "CREEPER_DEATH")
    end )
    if (thenEnd) then
        local map = {}
        for _,uuid in pairs(self.Deaths) do
            map[uuid] = true
        end
        for _,p in pairs(self:GetPlayers()) do
            if (not (map[p:GetUUID()])) then
                self:RecordDeath(self.Deaths, p:GetUUID())
                timer.Simple(0, function()
                    self:EndRoutine()
                end )
                break
            end
        end
    end
    return false
end

function MG:EndRoutine()
    if (not self.Started) then return end
    self.SuddenDeath = false
    self.Started = false
    local byUUID = {}
    for _,ply in pairs(self:GetPlayers()) do
        ply:ShowTitle("<green>Game Over!</green>", "")
        ply:SetGameMode(GM_SPECTATOR)
        ply:SendMessage("<gradient:yellow:gold:yellow>======================</gradient>")
        byUUID[ply:GetUUID()] = ply
    end
    self:BroadcastSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP")
    local place = 0
    for i=(#self.Deaths), 1, -1 do
        local uuid = self.Deaths[i]
        place = place + 1
        local p = byUUID[uuid]
        if (p == nil) then
            for _,q in pairs(server.GetPlayers()) do
                if (q:GetUUID() == uuid) then
                    p = q
                    break
                end
            end
        end
        local name
        if (p == nil) then
            name = "Unknown <gray>(Left the game)</gray>"
        else
            name = p:GetDisplayNameStripped()
            if (name == nil) then name = p:GetName() end
        end
        self:Broadcast("<b>[ <dark_aqua>" .. place .. "</dark_aqua> ]</b> <gradient:dark_blue:blue:dark_blue>" .. name .. "</gradient>")
    end
    self:Broadcast("<gradient:yellow:gold:yellow>======================</gradient>")
    local region = self:GetRegion()
    local center = region:GetCenterBlock():GetLocation()
    local world = region:GetWorld()
    local worldName = "nil"
    if (world ~= nil) then worldName = world:GetName() end
    local cx = center:GetX()
    local cy = center:GetY() + 32
    local cz = center:GetZ()
    local timerId = "ffa-explosions-" .. worldName .. "-" .. cx .. "-" + cz
    timer.Create(timerId, 0.25, 40, function()
        for q=1,3,1 do
            local x = cx + math.random(-32, 32)
            local y = cy + math.random(-32, 32)
            local z = cz + math.random(-32, 32)
            self:CreateExplosion(Location(world, x, y, z), 2 + q)
        end
    end )
    timer.Simple(10, function()
        timer.Remove(timerId)
        self:End()
    end )
end
