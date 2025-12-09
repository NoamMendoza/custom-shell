package com.codecrafters.shell.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utilidades para manejar rutas y búsqueda de ejecutables en el sistema.
 */
public class PathUtils {

    /**
     * Obtiene una lista de todos los ejecutables disponibles en el PATH del sistema.
     * Útil para el autocompletado.
     * 
     * @return Lista de nombres de ejecutables.
     */
    public static List<String> getExecutablesFromPath() {
        Set<String> executables = new HashSet<>();
        String path = System.getenv("PATH");
        if (path != null) {
            String[] pathDirs = path.split(File.pathSeparator);
            for (String dirPath : pathDirs) {
                File dir = new File(dirPath);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile() && file.canExecute()) {
                                executables.add(file.getName());
                            }
                        }
                    }
                }
            }
        }
        return new ArrayList<>(executables);
    }

    /**
     * Busca un ejecutable en el PATH del sistema.
     * 
     * @param command Nombre del comando a buscar.
     * @return Archivo ejecutable si se encuentra, o null si no existe. -
     * 
     */
    public static File findExecutable(String command) {
        String path = System.getenv("PATH");
        if (path == null) return null;
        
        // Log temporal para depuración de my_exe
        if (command.equals("my_exe")) {
            System.err.println("[DEBUG] Buscando: " + command);
            System.err.println("[DEBUG] PATH completo: " + path);
        }
        
        // Usar File.pathSeparator para compatibilidad multiplataforma
        String[] pathDirs = path.split(File.pathSeparator);
        for (String dirPath : pathDirs) {
            File dir = new File(dirPath);
            
            if (!dir.exists() || !dir.isDirectory()) {
                if (command.equals("my_exe")) {
                    System.err.println("[DEBUG] Directorio no existe o no es directorio: " + dirPath);
                }
                continue;
            }
            
            // Log temporal para my_exe
            if (command.equals("my_exe")) {
                System.err.println("[DEBUG] Buscando en: " + dirPath);
                File[] allFiles = dir.listFiles();
                if (allFiles != null) {
                    System.err.println("[DEBUG] Archivos en " + dirPath + ":");
                    for (File f : allFiles) {
                        System.err.println("[DEBUG]   - " + f.getName() + " (isFile=" + f.isFile() + ")");
                    }
                } else {
                    System.err.println("[DEBUG] listFiles() devolvió null para " + dirPath);
                }
            }
            
            // Listar archivos y buscar por nombre exacto
            // Esto evita problemas con caracteres especiales en nombres de archivo
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().equals(command) && file.isFile()) {
                        if (command.equals("my_exe")) {
                            System.err.println("[DEBUG] ¡ENCONTRADO! " + file.getAbsolutePath());
                        }
                        return file;
                    }
                }
            }
        }
        
        if (command.equals("my_exe")) {
            System.err.println("[DEBUG] NO ENCONTRADO en ningún directorio del PATH");
        }
        return null;
    }
}
