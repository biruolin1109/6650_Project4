import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Proposal implements Serializable {
    private long id;
    private String operation;

    public Proposal(long id, String operation) {
        this.id = id;
        this.operation = operation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public static synchronized Proposal generateProposal(String operation) {
        String s= new SimpleDateFormat("HHmmssSSS").format(new Date());
        Proposal proposal = new Proposal(Integer.parseInt(s), operation);
        return proposal;
    }
}
