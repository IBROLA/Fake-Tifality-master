package club.tifality.manager.command.impl;

import net.minecraft.util.EnumChatFormatting;
import club.tifality.Tifality;
import club.tifality.manager.command.Command;
import club.tifality.manager.command.CommandExecutionException;
import club.tifality.module.Module;
import club.tifality.property.Property;
import club.tifality.property.impl.EnumProperty;
import club.tifality.utils.Wrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ModuleCommand implements Command {

    @Override
    public String[] getAliases() {
        List<String> moduleAlises = new ArrayList<>();
        String[] alias = new String[moduleAlises.size()];
        Tifality.getInstance().getModuleManager().getModules().stream().filter(Objects::nonNull).filter(module ->
                !module.getElements().isEmpty()).forEach(module -> moduleAlises.add(module.getLabel().replaceAll(" ", "")));
        return moduleAlises.toArray(alias);
    }

    @Override
    public void execute(String[] arguments) throws CommandExecutionException {
        if (arguments.length != 3)
            throw new CommandExecutionException(getUsage());

        Module module = Tifality.getInstance().getModuleManager().getModule(arguments[0]);
        Property property = Property.getPropertyLabel(module, arguments[1]);

        if(property == null) {
            Wrapper.addChatMessage(EnumChatFormatting.RED + arguments[1] + " §7Invalid Property");
            return;
        }

        try {
            if (property.getType() == Boolean.class) {
                property.setValue(Boolean.parseBoolean(arguments[2]));
                Wrapper.addChatMessage("Property §6" + property.getLabel() + " §7set to §6" + property.getValue());
            } else if (property.getType() == Double.class) {
                property.setValue(Double.parseDouble(arguments[2]));
                Wrapper.addChatMessage("Property §6" + property.getLabel() + " §7set to §6" + property.getValue());
            } else {
                EnumProperty enumProperty = (EnumProperty) property;
                Arrays.stream(enumProperty.getValues()).filter(option -> option.name().equalsIgnoreCase(arguments[2])).forEach(option -> {
                    enumProperty.setValue(option);
                    Wrapper.addChatMessage("Property §6" + enumProperty.getLabel() + " §7set to §6" + enumProperty.getValue());
                });
            }
        } catch (Exception e) {
            Wrapper.addChatMessage(EnumChatFormatting.RED + arguments[2] + " §7Invalid Type");
        }
    }

    @Override
    public String getUsage() {
        return "<module> <property> <value>";
    }
}
