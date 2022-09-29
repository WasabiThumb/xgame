MG.Name = "Test Minigame"
MG.Description = "For testing purposes"
MG.MinPlayers = 1
MG.MaxPlayers = 1
MG.Schematic = "theater"
MG.GameMode = GM_ADVENTURE

print("Test Minigame: Loaded")

function MG:Start()
    print("Test Minigame: Starting")
    self:Broadcast("<gold>* This is a test minigame, it will close in 60 seconds</gold>")
    local me = self
    timer.Simple(60, function()
        me:End()
    end)
end

function MG:Tick()
    for _,ply in pairs(self:GetPlayers()) do
        particle.Start("REDSTONE", ply:GetPos())
        particle.SetOffset(0, 0, 0)
        particle.SetColor(255, 0, 255)
        particle.End()
    end
end

function MG:Stop()
    print("Test Minigame: Stopping")
end

function MG:Leave(ply)
    local name = ply:GetDisplayNameStripped()
    if (name == nil or name == '') then name = ply:GetName() end
    print("Test Minigame: Player (" .. name .. ") left instance")
end

function MG:OnDamage(ply, _, dmg)
    ply:SendMessage("<red>Took " .. dmg .. " damage</red>")
end

function MG:OnDeath(ply)
    ply:SendMessage("<dark_red>Died</dark_red>")
end

function MG:GetSpawn(_)
    local loc = self:GetRegion():GetCenterBlock():GetLocation()
    local world = loc:GetWorld()
    local x = loc:GetX()
    local y = loc:GetY()
    local z = loc:GetZ()
    loc = Location(world, x + math.random(-8, 8), y, z + math.random(-8, 8), loc:GetYaw(), loc:GetPitch())
    return loc
end

function MG:GetSpectatorSpawn()
    local loc = self:GetRegion():GetCenterBlock():GetLocation()
    loc:SetY(loc:GetY() + 6)
    return loc
end

function MG:GetSchematicPos()
    return self:GetRegion():GetCenterBlock():GetLocation()
end
