package com.artivisi.accountingfinance.util;

public final class DisplayLabels {

    private DisplayLabels() {
    }

    /**
     * Combobox label of the form "CODE - NAME", degrading gracefully when either
     * part is missing. Used by entity picker bindings (client/vendor comboboxes)
     * so data-initial-label renders the existing selection.
     */
    public static String codeName(String code, String name) {
        String safeCode = code == null ? "" : code;
        String safeName = name == null ? "" : name;
        if (safeCode.isEmpty() && safeName.isEmpty()) return "";
        if (safeCode.isEmpty()) return safeName;
        if (safeName.isEmpty()) return safeCode;
        return safeCode + " - " + safeName;
    }
}
