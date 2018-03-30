package journeymap.common.version;

import journeymap.common.*;
import java.util.*;
import com.google.common.base.*;

public class Version implements Comparable<Version>
{
    public final int major;
    public final int minor;
    public final int micro;
    public final String patch;
    
    public Version(final int major, final int minor, final int micro) {
        this(major, minor, micro, "");
    }
    
    public Version(final int major, final int minor, final int micro, final String patch) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.patch = ((patch != null) ? patch : "");
    }
    
    public static Version from(final String major, final String minor, final String micro, final String patch, Version defaultVersion) {
        Version result = null;
        try {
            if (!major.contains("@")) {
                result = new Version(parseInt(major), parseInt(minor), parseInt(micro), patch);
            }
        }
        catch (Exception e) {
            Journeymap.getLogger().warn(String.format("Version had problems when parsed: %s, %s, %s, %s", major, minor, micro, patch));
        }
        if (result == null) {
            if (defaultVersion == null) {
                defaultVersion = new Version(0, 0, 0);
            }
            result = defaultVersion;
        }
        return result;
    }
    
    public static Version from(final String versionString, Version defaultVersion) {
        try {
            final String[] strings = versionString.split("(?<=\\d)(?=\\p{L})");
            String[] majorMinorMicro = strings[0].split("\\.");
            final String patch = (strings.length == 2) ? strings[1] : "";
            if (majorMinorMicro.length < 3) {
                majorMinorMicro = Arrays.copyOf(strings, 3);
            }
            return from(majorMinorMicro[0], majorMinorMicro[1], majorMinorMicro[2], patch, defaultVersion);
        }
        catch (Exception e) {
            Journeymap.getLogger().warn(String.format("Version had problems when parsed: %s", versionString));
            if (defaultVersion == null) {
                defaultVersion = new Version(0, 0, 0);
            }
            return defaultVersion;
        }
    }
    
    private static int parseInt(final String number) {
        if (number == null) {
            return 0;
        }
        return Integer.parseInt(number);
    }
    
    public String toMajorMinorString() {
        return Joiner.on(".").join((Object)this.major, (Object)this.minor, new Object[0]);
    }
    
    public boolean isNewerThan(final Version other) {
        return this.compareTo(other) > 0;
    }
    
    public boolean isRelease() {
        return Strings.isNullOrEmpty(this.patch);
    }
    
    @Override
    public String toString() {
        return Joiner.on(".").join((Object)this.major, (Object)this.minor, new Object[] { this.micro + this.patch });
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Version version = (Version)o;
        return this.major == version.major && this.micro == version.micro && this.minor == version.minor && this.patch.equals(version.patch);
    }
    
    @Override
    public int hashCode() {
        int result = this.major;
        result = 31 * result + this.minor;
        result = 31 * result + this.micro;
        result = 31 * result + this.patch.hashCode();
        return result;
    }
    
    @Override
    public int compareTo(final Version other) {
        int result = Integer.compare(this.major, other.major);
        if (result == 0) {
            result = Integer.compare(this.minor, other.minor);
        }
        if (result == 0) {
            result = Integer.compare(this.micro, other.micro);
        }
        if (result == 0) {
            result = this.patch.compareToIgnoreCase(other.patch);
            if (result != 0) {
                if (this.patch.equals("")) {
                    result = 1;
                }
                if (other.patch.equals("")) {
                    result = -1;
                }
            }
        }
        return result;
    }
}
