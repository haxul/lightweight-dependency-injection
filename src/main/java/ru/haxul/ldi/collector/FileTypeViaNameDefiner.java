package ru.haxul.ldi.collector;

public class FileTypeViaNameDefiner implements FileTypeDefiner<String> {

    @Override
    public FileType define(String filename) {
        if (filename.endsWith(".class")) return FileType.CLASS;
        if (!filename.contains(".")) return FileType.FOLDER;
        return FileType.UNKNOWN;
    }
}
