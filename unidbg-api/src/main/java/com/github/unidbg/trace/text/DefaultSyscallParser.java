package com.github.unidbg.trace.text;

import com.github.unidbg.*;
import com.github.unidbg.arm.backend.Backend;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSyscallParser implements TraceCallParser {

    private final Map<Long, String> syscallMap = new HashMap<>();
    private final Map<String, String[]> syscallArgs = new HashMap<>();

    public DefaultSyscallParser() {
        // init map
        syscallMap.put(56L, "openat");
        syscallMap.put(57L, "close");
        syscallMap.put(63L, "read");
        syscallMap.put(64L, "write");
        syscallMap.put(98L, "futex");
        syscallMap.put(222L, "mmap");
        syscallMap.put(215L, "munmap");
        syscallMap.put(226L, "mprotect");

        // init args
        syscallArgs.put("openat", new String[] { "dirfd:int", "pathname:str", "flags:int", "mode:int" });
        syscallArgs.put("read", new String[] { "fd:int", "buf:ptr", "count:size" });
        syscallArgs.put("write", new String[] { "fd:int", "buf:ptr", "count:size" });
    }

    @Override
    public boolean canHandle(String moduleName, String symbolName, boolean isJni) {
        return "syscall".equals(moduleName) || (symbolName != null && symbolName.startsWith("syscall_"));
    }

    @Override
    public String formatCall(AssemblyCodeTextDumper.PendingCall call, Backend backend, boolean is64Bit, Emulator<?> emulator, AssemblyCodeTextDumper dumper) {
        String syscallName = call.funcName;
        if (syscallName.startsWith("syscall_")) {
            long syscallNum = Long.parseLong(syscallName.substring(8));
            syscallName = syscallMap.getOrDefault(syscallNum, syscallName);
            call.funcName = syscallName; // Upgrade the pending call internally
        }

        String[] argsTypes = syscallArgs.get(syscallName);
        StringBuilder sb = new StringBuilder("svc ").append(syscallName).append("(");
        String formatStr = null;

        if (argsTypes != null) {
            for (int i = 0; i < argsTypes.length; i++) {
                if (i > 0) sb.append(", ");
                String[] parts = argsTypes[i].split(":");
                String name = parts[0];
                String type = parts[1];
                
                long arg = dumper.getArgRegValue(backend, i, is64Bit);
                sb.append(dumper.formatArgValue(type, arg, backend));
                
                if ("format".equals(name) && "str".equals(type)) {
                    formatStr = dumper.readStringSafe(backend, arg);
                }
            }
            
            if (formatStr != null && (syscallName.contains("write") || syscallName.contains("syslog"))) {
                List<String> specifiers = dumper.extractFormatSpecifiers(formatStr);
                int argIndex = argsTypes.length;
                for (String spec : specifiers) {
                    sb.append(", ");
                    long arg = dumper.getArgRegValue(backend, argIndex++, is64Bit);
                    if (spec.endsWith("s")) {
                        sb.append(dumper.formatArgValue("str", arg, backend));
                    } else if (spec.endsWith("c")) {
                        sb.append(dumper.formatArgValue("char", arg, backend));
                    } else if (spec.endsWith("p")) {
                        sb.append(dumper.formatArgValue("ptr", arg, backend));
                    } else {
                        sb.append(arg).append(" (0x").append(Long.toHexString(arg)).append(")");
                    }
                }
            }
        } else {
            sb.append("...");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String formatReturnValue(AssemblyCodeTextDumper.PendingCall call, long retVal, Backend backend, boolean is64Bit, Emulator<?> emulator, AssemblyCodeTextDumper dumper) {
        if (retVal >= -1000 && retVal <= 1000) {
            return String.valueOf(retVal) + " (0x" + Long.toHexString(retVal) + ")";
        }
        return "0x" + Long.toHexString(retVal);
    }

    @Override
    public void printPostReturnMemoryDump(PrintStream out, AssemblyCodeTextDumper.PendingCall call, long retVal, Backend backend, boolean is64Bit, Emulator<?> emulator, AssemblyCodeTextDumper dumper) {
        // Syscall generally does not have complex post-memory dumps mapped standardly here,
        // unless you want to parse 'read' syscall destination buffer
    }
}
