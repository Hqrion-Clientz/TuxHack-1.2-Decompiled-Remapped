// 
// Decompiled by Procyon v0.5.36
// 

package nonapi.io.github.classgraph.utils;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import org.w3c.dom.Document;
import java.io.InputStream;
import java.nio.file.Path;
import java.net.URL;
import java.util.Properties;
import java.io.IOException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import io.github.classgraph.ClassGraph;

public final class VersionFinder
{
    private static final String MAVEN_PACKAGE = "io.github.classgraph";
    private static final String MAVEN_ARTIFACT = "classgraph";
    public static final OperatingSystem OS;
    public static final String JAVA_VERSION;
    public static final int JAVA_MAJOR_VERSION;
    public static final int JAVA_MINOR_VERSION;
    public static final int JAVA_SUB_VERSION;
    public static final boolean JAVA_IS_EA_VERSION;
    
    private VersionFinder() {
    }
    
    public static String getProperty(final String propName) {
        try {
            return System.getProperty(propName);
        }
        catch (SecurityException e) {
            return null;
        }
    }
    
    public static String getProperty(final String propName, final String defaultVal) {
        try {
            return System.getProperty(propName, defaultVal);
        }
        catch (SecurityException e) {
            return null;
        }
    }
    
    public static synchronized String getVersion() {
        final Class<?> cls = ClassGraph.class;
        try {
            final String className = cls.getName();
            final URL classpathResource = cls.getResource("/" + JarUtils.classNameToClassfilePath(className));
            if (classpathResource != null) {
                final Path absolutePackagePath = Paths.get(classpathResource.toURI()).getParent();
                final int packagePathSegments = className.length() - className.replace(".", "").length();
                Path path = absolutePackagePath;
                for (int i = 0; i < packagePathSegments && path != null; path = path.getParent(), ++i) {}
                for (int i = 0; i < 3 && path != null; ++i, path = path.getParent()) {
                    final Path pom = path.resolve("pom.xml");
                    try (final InputStream is = Files.newInputStream(pom, new OpenOption[0])) {
                        final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                        doc.getDocumentElement().normalize();
                        String version = (String)XPathFactory.newInstance().newXPath().compile("/project/version").evaluate(doc, XPathConstants.STRING);
                        if (version != null) {
                            version = version.trim();
                            if (!version.isEmpty()) {
                                return version;
                            }
                        }
                    }
                    catch (IOException ex) {}
                }
            }
        }
        catch (Exception ex2) {}
        try (final InputStream is2 = cls.getResourceAsStream("/META-INF/maven/io.github.classgraph/classgraph/pom.properties")) {
            if (is2 != null) {
                final Properties p = new Properties();
                p.load(is2);
                final String version2 = p.getProperty("version", "").trim();
                if (!version2.isEmpty()) {
                    return version2;
                }
            }
        }
        catch (IOException ex3) {}
        final Package pkg = cls.getPackage();
        if (pkg != null) {
            String version3 = pkg.getImplementationVersion();
            if (version3 == null) {
                version3 = "";
            }
            version3 = version3.trim();
            if (version3.isEmpty()) {
                version3 = pkg.getSpecificationVersion();
                if (version3 == null) {
                    version3 = "";
                }
                version3 = version3.trim();
            }
            if (!version3.isEmpty()) {
                return version3;
            }
        }
        return "unknown";
    }
    
    static {
        JAVA_VERSION = getProperty("java.version");
        int javaMajorVersion = 0;
        int javaMinorVersion = 0;
        int javaSubVersion = 0;
        final List<Integer> versionParts = new ArrayList<Integer>();
        if (VersionFinder.JAVA_VERSION != null) {
            for (final String versionPart : VersionFinder.JAVA_VERSION.split("[^0-9]+")) {
                try {
                    versionParts.add(Integer.parseInt(versionPart));
                }
                catch (NumberFormatException ex) {}
            }
            if (!versionParts.isEmpty() && versionParts.get(0) == 1) {
                versionParts.remove(0);
            }
            if (versionParts.isEmpty()) {
                throw new RuntimeException("Could not determine Java version: " + VersionFinder.JAVA_VERSION);
            }
            javaMajorVersion = versionParts.get(0);
            if (versionParts.size() > 1) {
                javaMinorVersion = versionParts.get(1);
            }
            if (versionParts.size() > 2) {
                javaSubVersion = versionParts.get(2);
            }
        }
        JAVA_MAJOR_VERSION = javaMajorVersion;
        JAVA_MINOR_VERSION = javaMinorVersion;
        JAVA_SUB_VERSION = javaSubVersion;
        JAVA_IS_EA_VERSION = (VersionFinder.JAVA_VERSION != null && VersionFinder.JAVA_VERSION.endsWith("-ea"));
        final String osName = getProperty("os.name", "unknown").toLowerCase(Locale.ENGLISH);
        if (osName == null) {
            OS = OperatingSystem.Unknown;
        }
        else if (osName.contains("mac") || osName.contains("darwin")) {
            OS = OperatingSystem.MacOSX;
        }
        else if (osName.contains("win")) {
            OS = OperatingSystem.Windows;
        }
        else if (osName.contains("nux")) {
            OS = OperatingSystem.Linux;
        }
        else if (osName.contains("sunos") || osName.contains("solaris")) {
            OS = OperatingSystem.Solaris;
        }
        else if (osName.contains("bsd")) {
            OS = OperatingSystem.Unix;
        }
        else if (osName.contains("nix") || osName.contains("aix")) {
            OS = OperatingSystem.Unix;
        }
        else {
            OS = OperatingSystem.Unknown;
        }
    }
    
    public enum OperatingSystem
    {
        Windows, 
        MacOSX, 
        Linux, 
        Solaris, 
        BSD, 
        Unix, 
        Unknown;
    }
}
