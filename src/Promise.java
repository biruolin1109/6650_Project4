import java.io.Serializable;

public class Promise implements Serializable {
    private String status;
    private Proposal proposal;

    public Promise(String status, Proposal proposal){
        this.status = status;
        this.proposal = proposal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

}
