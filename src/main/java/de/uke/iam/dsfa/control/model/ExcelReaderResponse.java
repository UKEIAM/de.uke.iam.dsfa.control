package de.uke.iam.dsfa.control.model;

import java.io.Serializable;
import java.util.List;

public class ExcelReaderResponse implements Serializable {
    String status;
    List<String> comments;

    public ExcelReaderResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }
}
