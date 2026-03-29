package com.github.unidbg.trace.text;

import com.github.unidbg.*;
import com.github.unidbg.arm.backend.Backend;
import java.io.PrintStream;

/**
 * Interface for parsing and formatting function calls and their arguments/return values.
 */
public interface TraceCallParser {

    /**
     * Returns true if this parser can handle the provided module and symbol.
     * Often checks moduleName against "libc.so", "libc++.so" or matches typical names like "sys_" or JNI calls.
     */
    boolean canHandle(String moduleName, String symbolName, boolean isJni);

    /**
     * Formats the function call (including resolving arguments).
     * @return Formatted string (e.g., "libc.so::printf(\"%s\", \"hello\")"), or null to skip/fallback.
     */
    String formatCall(AssemblyCodeTextDumper.PendingCall call, Backend backend, boolean is64Bit, Emulator<?> emulator, AssemblyCodeTextDumper dumper);

    /**
     * Formats the return value when the function returns.
     */
    String formatReturnValue(AssemblyCodeTextDumper.PendingCall call, long retVal, Backend backend, boolean is64Bit, Emulator<?> emulator, AssemblyCodeTextDumper dumper);

    /**
     * Optionally dump updated memory (like modified buffers for sprint / memcpy) after function return.
     * Do nothing if not applicable.
     */
    void printPostReturnMemoryDump(PrintStream out, AssemblyCodeTextDumper.PendingCall call, long retVal, Backend backend, boolean is64Bit, Emulator<?> emulator, AssemblyCodeTextDumper dumper);
}
