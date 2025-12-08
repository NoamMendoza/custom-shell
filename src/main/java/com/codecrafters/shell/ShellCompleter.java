package com.codecrafters.shell;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementación de Completer para JLine.
 * Proporciona autocompletado para comandos builtin y ejecutables en el PATH.
 * Maneja la lógica de doble tabulación para mostrar todas las opciones.
 */
public class ShellCompleter implements Completer {
    private final List<String> commands;
    private String lastWord = null;
    private int tabPressCount = 0;

    /**
     * Crea un nuevo completer con la lista de comandos disponibles.
     * @param commands Lista de nombres de comandos para autocompletar.
     */
    public ShellCompleter(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String word = line.word();
        if (word == null) {
            word = "";
        }

        if (!word.equals(this.lastWord)) {
            this.lastWord = word;
            this.tabPressCount = 0;
        }
        this.tabPressCount++;

        List<String> matches = new ArrayList<>();
        for (String cmd : commands) {
            if (cmd.startsWith(word)) {
                matches.add(cmd);
            }
        }
        
        if (matches.isEmpty()) {
            return;
        }
        
        // Add candidates for JLine to handle completion
        for (String match : matches) {
            candidates.add(new Candidate(match));
        }

        if (matches.size() > 1) {
            if (this.tabPressCount == 1) {
                // On first press with multiple candidates, clear to ring bell
                candidates.clear();
            } else {
                // On second press, provide all candidates but handle display manually
                candidates.clear();
                
                Collections.sort(matches);
                
                reader.getTerminal().writer().println();
                reader.getTerminal().writer().print(String.join("  ", matches));
                reader.getTerminal().writer().println();
                reader.callWidget(LineReader.REDRAW_LINE);
                reader.getTerminal().writer().flush();
                
                // Reset for next completion cycle
                this.tabPressCount = 0;
            }
        }
    }
}
