package com.github.unidbg;

import java.io.PrintStream;

public interface TraceHook {

    void setRedirect(PrintStream redirect);

    void stopTrace();

    default void setDebugSymbolResolution(boolean debugSymbolResolution) {
    }

    default TraceHook setDisableSMC(boolean disableSMC) {
        return this;
    }

    default TraceHook setDisableHexdump(boolean disableHexdump) {
        return this;
    }

    default TraceHook setDisableFunctionCall(boolean disableFunctionCall) {
        return this;
    }
}
