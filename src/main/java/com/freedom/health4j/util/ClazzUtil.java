package com.freedom.health4j.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * the util of class.
 * Created by yanghua on 1/21/15.
 */
public class ClazzUtil {

    public static final Log logger = LogFactory.getLog(ClazzUtil.class);

    public static void findAndAddClassesInPackageByFile(String packageName,
                                                        String packagePath,
                                                        final boolean recursive,
                                                        Set<Class> classes,
                                                        String compareClassName) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.warn("the package :" + packageName + "has no files");
            return;
        }

        File[] dirFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (recursive && pathname.isDirectory()) ||
                    (pathname.getName().endsWith(".class"));
            }
        });

        for (File file : dirFiles) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                                                 file.getAbsolutePath(),
                                                 recursive,
                                                 classes,
                                                 compareClassName);
            } else {
                String className = file.getName().substring(0, file.getName().length() - 6);

                try {
                    Class clazz = Thread.currentThread().
                        getContextClassLoader().loadClass(packageName + "." + className);

                    //fetch the class that implemented the interface `IService`
                    Class[] ifClasses = clazz.getSuperclass().getInterfaces();
                    if (ifClasses == null || ifClasses.length == 0) {
                        continue;
                    }

                    for (Class ifclass : ifClasses) {
                        if (ifclass.getName().equals(compareClassName)) {
                            classes.add(clazz);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error("[findAndAddClassesInPackageByFile] occurs a ClassNotFoundException : "
                                         + e.getMessage());
                    }
                    throw new RuntimeException(e);
                }
            }

        }
    }


    public static Set<Class> traverse(String packageStr, Class searchClazz) {
        Set<Class> classes = new LinkedHashSet();
        boolean recursive = true;
        String packageDirName = packageStr.replace('.', '/');

        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    //get physical path
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    ClazzUtil.findAndAddClassesInPackageByFile(packageStr, filePath, recursive, classes,
                                                               searchClazz.getName());
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    String packageName = packageStr;
                    jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();

                        if (name.charAt(0) == '/') {
                            name = name.substring(1);
                        }

                        if (name.startsWith(packageDirName)) {
                            int idx = name.lastIndexOf('/');

                            if (idx != -1) {
                                packageName = name.substring(0, idx).replace('/', '.');
                            }

                            if ((idx != -1) || recursive) {
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);

                                    try {
                                        classes.add((Class) Class.forName(packageName + "." + className));
                                    } catch (ClassNotFoundException e) {
                                        if (logger.isErrorEnabled()) {
                                            logger.error("occurs a ClassNotFoundException : " + e.toString());
                                        }

                                        throw new RuntimeException("occurs a ClassNotFoundException : "
                                                                       + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("[scan] occurs a IOException : " + e.getMessage());
            }
            throw new RuntimeException(e);
        }

        return classes;
    }

}
