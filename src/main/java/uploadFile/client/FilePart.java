package uploadFile.client;

import java.io.File;

public class FilePart {
    private String name;
    private String fileName;
    private String contentType;
    private File file;
    private long fileSize;
    private StringBuilder prev;
    private String end;

    public FilePart(String name, String fileName, String contentType, File file) {
        this.name = name;
        this.fileName = fileName;
        this.contentType = contentType;
        this.file = file;
        this.fileSize = file.length();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public StringBuilder getPrev() {
        return prev;
    }

    public void setPrev(StringBuilder prev) {
        this.prev = prev;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
