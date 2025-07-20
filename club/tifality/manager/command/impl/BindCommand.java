package club.tifality.manager.command.impl;

import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import club.tifality.Tifality;
import club.tifality.manager.command.Command;
import club.tifality.manager.command.CommandExecutionException;
import club.tifality.module.Module;
import club.tifality.utils.Wrapper;

public class BindCommand implements Command {

    @Override
    public String[] getAliases() {
        return new String[]{"bind", "b", "keybind"};
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if(arguments.length != 3)
            throw new CommandExecutionException(getUsage());

        Module module = Tifality.getInstance().getModuleManager().getModule(arguments[1]);
        int key = Keyboard.getKeyIndex(arguments[2].toUpperCase());

        if(module == null) {
            Wrapper.addChatMessage(EnumChatFormatting.RED + arguments[1] + " §7is an invalid module");
            return;
        }

        module.setKey(key);
        Wrapper.addChatMessage(String.format("Bound §6%s §7to §6%s", module.getLabel(), key == 0 ? "none" : arguments[2].toUpperCase()));
    }

    @Override
    public String getUsage() {
        return "bind/b/keybind <module> <key>";
    }
}
