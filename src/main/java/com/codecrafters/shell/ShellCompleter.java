package com.codecrafters.shell;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de Completer para JLine.
 * Proporciona autocompletado para comandos builtin y ejecutables en el PATH.
 * Maneja la lógica de doble tabulación para mostrar todas las opciones.
 */
public class ShellCompleter implements Completer {
    private final StringsCompleter delegate;
    private String lastWord = null;
    private int tabPressCount = 0;

    /**
     * Crea un nuevo completer con la lista de comandos disponibles.
     * @param commands Lista de nombres de comandos para autocompletar.
     */
    public ShellCompleter(List<String> commands) {
        this.delegate = new StringsCompleter(commands);
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

        List<Candidate> delegateCandidates = new ArrayList<>();
        this.delegate.complete(reader, line, delegateCandidates);

        if (delegateCandidates.size() > 1) {
            if (this.tabPressCount == 1) {
                // On first press with multiple candidates, do nothing.
                // This should cause the bell to ring.
                candidates.clear();
            } else {
                // On second press, provide all candidates.
                // jline should then display them.
                candidates.addAll(delegateCandidates);
                // Reset for next completion cycle
                this.tabPressCount = 0;
            }
        } else {
            // 0 or 1 candidate, let jline handle it normally.
            candidates.addAll(delegateCandidates);
        }
    }
}
