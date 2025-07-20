package club.tifality.module;

import club.tifality.Tifality;
import club.tifality.module.impl.combat.*;
import club.tifality.module.impl.movement.*;
import club.tifality.module.impl.other.*;
import club.tifality.module.impl.player.*;
import club.tifality.module.impl.render.*;
import com.google.common.collect.ImmutableClassToInstanceMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class ModuleManager {

    private final ImmutableClassToInstanceMap<Module> instanceMap;

    public ModuleManager() {
        instanceMap = putInInstanceMap(
                // Combat
                new AntiBot(), new AntiVelocity(), new AutoClicker(), new AutoHeal(), new AutoPot(), new Criticals(), new KillAura(), new Reach(), new Regen(),
                //Movement
                new AntiFall(), new Disabler(), new Flight(), new Jesus(), new LongJump(), new NoSlowdown(), new Phase(), new Speed(), new Sprint(), new Step(), new TargetStrafe(),
                //Other
                new HackerDetect(), new Animations(), new AutoBow(), new AutoHypixel(), new BetterChat(), new ChatBypass(), new GameSpeed(), new Killsults(), new MemoryFix(), new SilentView(),
                //Player
                new AntiObby(), new AutoArmor(), new AutoTool(), new ChestStealer(), new FastUse(), new InventoryCleaner(), new InventoryMove(), new NoFall(), new NoRotate(), new Scaffold(), new SpeedMine(), new StreamerMode(),
                //Render
                new BlockOutline(), new Brightness(), new Chams(), new ChestESP(), new ChinaHat(), new Crosshair(), new DamageParticles(), new Debug(), new EnchantEffect(), new ESP(), new Health(), new Hitmarkers(), new Hud(), new Indicators(), new NameTag(), new NoHurtCamera(), new TargetHUD(), new TimeChanger());

        getModules().forEach(Module::reflectProperties);

        getModules().forEach(Module::resetPropertyValues);

        Tifality.getInstance().getEventBus().subscribe(this);
    }

    public void postInit() {
        getModules().forEach(Module::resetPropertyValues);
    }

    private ImmutableClassToInstanceMap<Module> putInInstanceMap(Module... modules) {
        ImmutableClassToInstanceMap.Builder<Module> modulesBuilder = ImmutableClassToInstanceMap.builder();
        Arrays.stream(modules).forEach(module -> modulesBuilder.put((Class<Module>) module.getClass(), module));
        return modulesBuilder.build();
    }

    public Collection<Module> getModules() {
        return instanceMap.values();
    }

    public <T extends Module> T getModule(Class<T> moduleClass) {
        return instanceMap.getInstance(moduleClass);
    }

    public Module getModule(String label) {
        return getModules().stream().filter(module -> module.getLabel().replaceAll(" ", "").equalsIgnoreCase(label)).findFirst().orElse(null);
    }

    public static <T extends Module> T getInstance(Class<T> clazz) {
        return Tifality.getInstance().getModuleManager().getModule(clazz);
    }

    public List<Module> getModulesForCategory(ModuleCategory category) {
        return getModules().stream()
                .filter(module -> module.getCategory() == category)
                .collect(Collectors.toList());
    }
}