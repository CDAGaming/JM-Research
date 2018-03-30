package journeymap.client.mod;

import org.apache.logging.log4j.*;
import net.minecraft.block.properties.*;
import java.lang.reflect.*;
import net.minecraftforge.fml.relauncher.*;
import journeymap.common.*;
import journeymap.common.log.*;
import net.minecraft.block.state.*;
import javax.annotation.*;
import java.util.*;

@ParametersAreNonnullByDefault
public class ModPropertyEnum<T>
{
    private static final Logger logger;
    private final boolean valid;
    private final PropertyEnum propertyEnum;
    private final Method method;
    
    public ModPropertyEnum(final PropertyEnum propertyEnum, final Method method, final Class<T> returnType) {
        this.valid = (propertyEnum != null && method != null);
        this.propertyEnum = propertyEnum;
        this.method = method;
    }
    
    public ModPropertyEnum(final PropertyEnum propertyEnum, final String methodName, final Class<T> returnType, final Class<?>[] methodArgTypes) {
        this(propertyEnum, lookupMethod(propertyEnum, methodName, (Class[])methodArgTypes), returnType);
    }
    
    public ModPropertyEnum(final String declaringClassName, final String propertyEnumStaticFieldName, final String methodName, final Class<T> returnType) {
        this(declaringClassName, propertyEnumStaticFieldName, methodName, returnType, new Class[0]);
    }
    
    public ModPropertyEnum(final String declaringClassName, final String propertyEnumStaticFieldName, final String methodName, final Class<T> returnType, final Class<?>[] methodArgTypes) {
        this(lookupPropertyEnum(declaringClassName, propertyEnumStaticFieldName), methodName, returnType, methodArgTypes);
    }
    
    public ModPropertyEnum(final String declaringClassName, final String propertyEnumStaticFieldName, final Method method, final Class<T> returnType) {
        this(lookupPropertyEnum(declaringClassName, propertyEnumStaticFieldName), method, returnType);
    }
    
    public static PropertyEnum lookupPropertyEnum(final String declaringClassName, final String propertyEnumStaticFieldName) {
        try {
            final Class declaringClass = Class.forName(declaringClassName);
            return (PropertyEnum)ReflectionHelper.findField(declaringClass, new String[] { propertyEnumStaticFieldName }).get(declaringClass);
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error reflecting PropertyEnum on %s.%s: %s", (Object)declaringClassName, (Object)propertyEnumStaticFieldName, (Object)LogFormatter.toPartialString(e));
            return null;
        }
    }
    
    public static Method lookupMethod(final PropertyEnum propertyEnum, final String methodName, final Class... methodArgTypes) {
        if (propertyEnum != null) {
            return lookupMethod(propertyEnum.func_177699_b().getName(), methodName, methodArgTypes);
        }
        return null;
    }
    
    public static Method lookupMethod(final String declaringClassName, final String methodName, final Class... methodArgTypes) {
        try {
            final Class declaringClass = Class.forName(declaringClassName);
            return ReflectionHelper.findMethod(declaringClass, methodName, (String)null, methodArgTypes);
        }
        catch (Exception e) {
            Journeymap.getLogger().error("Error reflecting method %s.%s(): %s", (Object)declaringClassName, (Object)methodName, (Object)LogFormatter.toPartialString(e));
            return null;
        }
    }
    
    public PropertyEnum getPropertyEnum() {
        return this.propertyEnum;
    }
    
    public boolean isValid() {
        return this.valid;
    }
    
    @Nullable
    public T getValue(final IBlockState blockState, @Nullable final Object... args) {
        if (this.valid) {
            try {
                final Comparable<?> enumValue = (Comparable<?>)blockState.func_177228_b().get((Object)this.propertyEnum);
                if (enumValue != null) {
                    return (T)this.method.invoke(enumValue, args);
                }
            }
            catch (Exception e) {
                ModPropertyEnum.logger.error("Error using mod PropertyEnum: " + LogFormatter.toPartialString(e));
            }
        }
        return null;
    }
    
    @Nullable
    public static <T> T getFirstValue(final Collection<ModPropertyEnum<T>> modPropertyEnums, final IBlockState blockState, @Nullable final Object... args) {
        for (final ModPropertyEnum<T> modPropertyEnum : modPropertyEnums) {
            final T result = modPropertyEnum.getValue(blockState, args);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    static {
        logger = Journeymap.getLogger();
    }
}
