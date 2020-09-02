/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fivegex.monitoring.control.probescatalogue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author uceeftu
 */
public abstract class AbstractProbeCatalogue {
    String probesPackage;
    String probesSuffix;
    Set <Class> probeClasses;
    

    public AbstractProbeCatalogue(String probesPackage, String probesSuffix) {
        this.probesPackage = probesPackage.replace(".", "/");
        this.probesSuffix = probesSuffix;
        this.probeClasses = new HashSet <>();
        
    }
    
    
    private void searchForProbesInSubDirectory(File directory) throws ClassNotFoundException, IOException{
        if (directory.isDirectory()) {
                String[] files = directory.list();
                directory.listFiles();
                for (String file : files) {
                    if (file.contains(probesSuffix) && file.endsWith(".class")) {
                        String probesPackagePath = this.probesPackage.replace("/", ".");
                        String packageNameSpace = directory.getPath().replace("/", ".");
                        String [] nameSpaceSplit = packageNameSpace.split(probesPackagePath);
                        String dirLeafName;
                        
                        if (nameSpaceSplit.length > 1)
                            dirLeafName = packageNameSpace.split(probesPackagePath)[1];
                        else
                            dirLeafName = "";
                        
                        Class entryClass = Class.forName(probesPackagePath + dirLeafName + "." + file.substring(0, file.length() - 6));
                        probeClasses.add(entryClass);
                        System.out.println(entryClass.getName());
                    }
                    else {
                         File subDirectory = new File(directory + "/" + URLDecoder.decode(file, "UTF-8"));
                         searchForProbesInSubDirectory(subDirectory);
                    }
                }
            }
    }
    
    private void searchForProbesInDirectories() throws ClassNotFoundException, IOException {
        System.out.println("Search for Probes in directories:");
        
        ClassLoader cld = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = cld.getResources(this.probesPackage);
        
        while (resources.hasMoreElements()) {
            String path = resources.nextElement().getPath();
            File directory = new File(URLDecoder.decode(path, "UTF-8"));
            searchForProbesInSubDirectory(directory);
        }
    }
    
    
    private void searchForProbesInJars() throws ClassNotFoundException, IOException {
        System.out.println("Search for Probes in jars:");
        String jarPath; 
        URL[] jarURLArray = ((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();
        
        if (jarURLArray == null) {
            throw new ClassNotFoundException("Can't get class loader.");
        } 
        else if (jarURLArray.length > 1) 
                return; //we look in the whole single jar only
        else {
            jarPath = jarURLArray[0].getPath(); // we consider the current single jar only from the array
            System.out.println(jarPath);
        }
        JarFile jarFile = null;

        jarFile = new JarFile(jarPath);
        Enumeration<JarEntry> en = jarFile.entries();
        while (en.hasMoreElements()) {
            JarEntry entry = en.nextElement();
            String entryName = entry.getName();
            // although directories are hierarchical java packages shouldn't be: looking inside p1.p2.p3 should not include p1.p2.p3.p4
            // however here we will look for Probes having the ProbesSuffix suffix and matching the probesPackage namespace
            //if (entryName != null && entryName.matches(probesPackage + "/[^/|\\.]*\\.class")) { // this will look only in probesPackage
            if (entryName != null && entryName.matches(probesPackage + "/.*" + probesSuffix + "\\.class")) {  // we look for the Probessuffix only in probesPackage.* 
                    System.out.println("entryName: "  + entryName);
                    Class entryClass = Class.forName(entryName.substring(0, entryName.length() - 6).replace("/", "."));
                    if (entryClass != null) {
                        //System.out.println(entryClass.getName());
                        probeClasses.add(entryClass);
                    }
            }
        }    
    }
    
    
    public void SearchForProbes() throws ClassNotFoundException, IOException {
        this.searchForProbesInDirectories();
        this.searchForProbesInJars();
    }
    
}
