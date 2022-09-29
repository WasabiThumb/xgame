MG.Name = "King of the Hill"
MG.Description = "Stay at the top of the hill as long as possible for the most points"
MG.MinPlayers = 2
MG.MaxPlayers = 16
MG.Schematic = "koth"
MG.GameMode = GM_ADVENTURE
MG.Icon = "GOLD_BLOCK"

MG.Started = false
MG.CaptureTimes = {}
MG.VoidKills = {}

MG.EndBar = nil
MG.StartTime = 0
MG.IsDoingEndRoutine = false
MG.ParticleIncrement = 50

MG.RocketUses = {}

function MG:Start()
    self:Broadcast("<gold>* Game starts in 10 seconds</gold>")
    self:BroadcastSound("BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING", "NOTE_PLING")
    timer.Simple(10, function()
        local players = self:GetPlayers()
        local bar = BossBar("Timer")
        bar:SetPlayers(players)
        bar:SetProgress(1)
        self.EndBar = bar
        self.StartTime = os.time()
        for _,ply in ipairs(self:GetPlayers()) do
            ply:SendMessage("<green>* The game has started!</green>")
            self:Loadout(ply)
        end
        self:BroadcastSound("ENTITY_ENDER_DRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL", "ENDERDRAGON_GROWL")
        self.Started = true
    end )
end

MG.LastTick = -1
MG.LastWinner = nil
MG.LastWinnerUUID = ""
function MG:Tick()
    local now = os.time()
    if (self.LastTick < 0) then
        self.LastTick = now
        return
    end
    local diff = math.abs(os.difftime(now, self.LastTick))
    self.LastTick = now
    if (not self.Started) then return end
    local prog = math.abs(os.difftime(now, self.StartTime))
    local pc = 1 - (prog / 180)
    if (pc < 0) then
        self.EndBar:SetPlayers({})
        self:RoundOverRoutine()
        return
    else
        self.EndBar:SetProgress(math.min(pc, 1))
    end
    local origin = self:GetSchematicPos():ToVector()
    for _,ply in ipairs(self:GetPlayers()) do
        if (ply:GetGameMode() == GM_SPECTATOR) then return end
        local pos = ply:GetPos():ToVector()
        local dist = pos:DistanceSqr(origin)
        if (dist <= 5) then
            local uuid = ply:GetUUID()
            local curTime = self.CaptureTimes[uuid]
            if (curTime == nil) then curTime = 0 end
            curTime = curTime + diff
            self.CaptureTimes[uuid] = curTime
        end
    end
    local winner = self:GetWinner()
    if (winner ~= nil) then
        if (winner:GetUUID() ~= self.LastWinnerUUID) then
            local nm = winner:GetDisplayName()
            if (nm == nil) then nm = winner:GetName() end
            self:Broadcast("<green>* The lead has went to " .. nm .. "!</green>")
            self:BroadcastSound("BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING", "NOTE_PLING")
        end
        if (winner:GetGameMode() ~= GM_SPECTATOR) then
            if (self.ParticleIncrement > 5) then
                self.ParticleIncrement = 0
                local crownPos = winner:GetPos()
                crownPos:SetY(crownPos:GetY() + 2.25)
                for r=1,11,1 do
                    local ang = ((r - 1) / 11) * 2 * math.pi
                    local maxOff = 0
                    if ((r == 1) or (r == 3) or (r == 5) or (r == 7) or (r == 9)) then
                        maxOff = 0.2
                    end
                    for yOff=0,maxOff,0.1 do
                        local particlePos = Location(
                                crownPos:GetWorld(),
                                crownPos:GetX() + (0.4 * math.cos(ang)),
                                crownPos:GetY() + yOff,
                                crownPos:GetZ() + (0.4 * math.sin(ang))
                        )
                        particle.Start("REDSTONE", particlePos)
                        particle.SetOffset(0, 0, 0)
                        particle.SetColor(255, 255, 0)
                        particle.End()
                    end
                end
            else
                self.ParticleIncrement = self.ParticleIncrement + 1
            end
        end
        self.LastWinner = winner
        self.LastWinnerUUID = winner:GetUUID()
    end
end

function MG:Stop()
end

-- Events
function MG:OnChat()
    if (self.IsDoingEndRoutine) then return true end
end

function MG:OnMove(ply, from, to)
    if (not self.Started) then return end
    local y = to:GetY()
    if (y < 10) then
        if (self.Started and (ply:GetGameMode() ~= GM_SPECTATOR)) then
            table.insert(self.VoidKills, ply:GetUUID())
            ply:Damage(10000)
        else
            ply:TeleportAsync(self:GetSpawn(ply))
        end
    end
end

function MG:OnBreak(ply, block)
    if (not self.Started) then return true end
    return false
end

function MG:OnDamage(ply, atk, dmg)
    if (not self.Started) then return true end
    if (atk ~= nil) then
        return false
    end
    if ((#self.VoidKills) > 0) then
        local uuid = ply:GetUUID()
        local key = -1
        for k,v in pairs(self.VoidKills) do
            if (v == uuid) then
                key = k
                break
            end
        end
        if (key >= 0) then
            table.remove(self.VoidKills, key)
            return false
        end
    end
    return true
end

function MG:OnDeath(ply)
    ply:ShowTitle("<red>You died!</red>", "<dark_red>Respawning in 5 seconds</dark_red>")
    self:BroadcastSound("ENTITY_CREEPER_DEATH", "CREEPER_DEATH")
    timer.Simple(5, function()
        self:Loadout(ply)
    end )
end

function MG:OnInteract(ply, item, leftClick, _)
    if (leftClick) then return false end
    if (not self.Started) then return false end
    if (item == nil) then return false end
    if (string.lower(item:GetMaterial():GetName()) == "iron_horse_armor") then
        local now = os.time()
        local elapsed
        local uuid = ply:GetUUID()
        local previous = self.RocketUses[uuid]
        if (previous ~= nil) then
            elapsed = math.abs(os.difftime(previous, now))
        else
            elapsed = 9999
        end
        if (elapsed > 30) then
            self.RocketUses[uuid] = now
            local pos = ply:GetPos()
            local world = pos:GetWorld()
            local dir = pos:GetDirection()
            local sx = pos:GetX() + (dir:GetX() * 0.3)
            local sy = pos:GetY() + (dir:GetY() * 0.3) + 1.5
            local sz = pos:GetZ() + (dir:GetZ() * 0.3)
            local loc
            local travelled = 0
            local hitBlock
            while (travelled <= 92) do
                loc = Location(world, sx, sy, sz)
                particle.Start("REDSTONE", loc)
                particle.SetOffset(0, 0, 0)
                particle.SetColor(255, 255, 255)
                particle.End()
                local block = loc:GetBlock()
                if (string.lower(block:GetMaterial():GetName()) ~= "air") then
                    hitBlock = block
                    break
                end
                sx = sx + (dir:GetX() * 0.5)
                sy = sy + (dir:GetY() * 0.5)
                sz = sz + (dir:GetZ() * 0.5)
                travelled = travelled + 0.5
            end
            if (hitBlock == nil) then
                self:BroadcastSound("BLOCK_DISPENSER_FAIL", "WOOD_CLICK")
            else
                local locVec = loc:ToVector()
                for _,p in ipairs(world:GetNearbyPlayers("PLAYER", loc, 6)) do
                    local pLoc = p:GetPos()
                    local pLocVec = pLoc:ToVector()
                    local mag = math.min(math.max(12 / locVec:Distance(pLocVec), 1), 12)
                    local norm = pLocVec:Subtract(locVec)
                    local normLength = math.max(norm:Length(), 0.1) -- attempt to index ? (a nil value)
                    local mult = mag / normLength
                    p:SetVelocity(p:GetVelocity():Add(norm:Multiply(Vector(mult, mult, mult))))
                end
                self:BroadcastSound("ENTITY_GENERIC_EXPLODE", "EXPLODE")
                self:CreateExplosion(loc, 2)
            end
        else
            local remaining = math.ceil(30 - elapsed)
            ply:SendMessage("<red>* </red><gradient:#b50009:#ed0004:#b50009>You need to wait <b><gold>" .. remaining .. "s</gold></b> before using this again</gradient>")
        end
        return true
    end
    return false
end

-- Setup
function MG:GetSpawn(_)
    local center = self:GetRegion():GetCenterBlock():GetLocation()
    local world = center:GetWorld()
    local x = center:GetX()
    local y = center:GetY()
    local z = center:GetZ()
    local angle = math.random() * 2 * math.pi
    local dist = (math.random() * 6) + 10
    local nx = math.floor((math.cos(angle) * dist) + x) + 0.5
    local nz = math.floor((math.sin(angle) * dist) + z) + 0.5
    local ny = y
    for ty=y,0,-1 do
        local block = Location(world, nx, ty, nz):GetBlock()
        if (string.lower(block:GetMaterial():GetName()) ~= "air") then
            ny = (ty + 1)
            break
        end
    end
    return Location(world, nx, ny, nz)
end

function MG:GetSchematicPos()
    return self:GetRegion():GetCenterBlock():GetLocation()
end

-- Internal
function MG:Loadout(ply)
    ply:SetGameMode(GM_SURVIVAL)
    ply:TeleportAsync(self:GetSpawn(ply))
    self:GiveEffect(ply, 6000, 2, "SPEED")
    self:GiveEffect(ply, 6000, 3, "JUMP")
    local inv = ply:GetInventory()
    inv:SetItem(0, ItemStack(Material("WOODEN_SWORD"), 1))
    inv:SetItem(1, ItemStack(Material("WOODEN_SHOVEL"), 1))
    inv:SetItem(2, ItemStack(Material("SLIME_BLOCK"), 8))
    inv:SetItem(3, ItemStack(Material("COBBLESTONE"), 32))
    inv:SetItem(4, ItemStack(Material("COOKED_BEEF"), 16))
    local rpg = ItemStack(Material("IRON_HORSE_ARMOR"), 1)
    rpg:SetDisplayName("<i:false><red>RPG Launcher</red></i>")
    inv:SetItem(5, rpg)
end

function MG:GetWinnerUUID()
    local maxUUID
    local maxTime = -1
    for k,v in pairs(self.CaptureTimes) do
        if (v > maxTime) then
            maxUUID = k
            maxTime = v
        end
    end
    return maxUUID
end

function MG:GetWinner()
    local uuid = self:GetWinnerUUID()
    if (uuid == nil) then return nil end
    for _,ply in ipairs(self:GetPlayers()) do
        if (ply:GetUUID() == uuid) then return ply end
    end
    return nil
end

function MG:RoundOverRoutine()
    self.Started = false
    for _,ply in ipairs(self:GetPlayers()) do
        ply:SetGameMode(GM_SPECTATOR)
        ply:SendMessage("<red>Round Over!</red>")
    end
    self:BroadcastSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP")
    local scores = {}
    local keys = {}
    for uuid,time in pairs(self.CaptureTimes) do
        local ply
        for _,p in ipairs(self:GetPlayers()) do
            if (p:GetUUID() == uuid) then
                ply = p
                break
            end
        end
        if (ply == nil) then goto continue end
        local set = scores[time]
        if (set == nil) then
            scores[time] = { ply }
            table.insert(keys, time)
        else
            table.insert(set, ply)
            scores[time] = set
        end
        ::continue::
    end
    table.sort(keys, function(a, b) return a > b end)
    if ((#keys) < 1) then
        self:Broadcast("<dark_red>Nobody captured the hill during the course of the game, meaning no winner can be determined.</dark_red>")
        timer.Simple(3, function()
            self:End()
        end )
        return
    end
    local delay = 3
    for place,score in ipairs(keys) do
        if (place > 3) then break end
        local step = math.max(score / 25, 1)
        local reps = math.ceil(score / step)
        local value = 0
        for rep=0,reps,1 do
            value = math.min(value + step, score)
            local fv = value
            if (rep < reps) then
                fv = math.floor(fv)
            end
            timer.Simple(delay, function()
                for _,ply in ipairs(self:GetPlayers()) do
                    ply:SendActionBar("<gold>" .. fv .. "</gold>")
                end
                self:BroadcastSound("BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING", "NOTE_PLING")
            end )
            delay = delay + ((rep / reps) * (0.35)) + 0.05
        end
        timer.Simple(delay, function()
            for _,player in ipairs(scores[score]) do
                local nm = player:GetDisplayName()
                if (nm == nil) then nm = player:GetName() end
                self:Broadcast("<b><dark_aqua>" .. place .. "</dark_aqua></b>. <b><aqua>" .. nm .. "</aqua></b> (<gold>" .. score .. "</gold>)")
            end
            self:BroadcastSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP")
        end )
        delay = delay + 2
    end
    delay = delay + 2
    timer.Simple(delay, function()
        self:End()
    end )
end