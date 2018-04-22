import javax.servlet.*;
import javax.servlet.sip.*;

public class ProxyServlet extends SipServlet implements SipErrorListener, Servlet {

    /** Creates a new instance of ProxyServlet. */
    public ProxyServlet() {}

    @Override
    protected void doInvite(SipServletRequest request) {
        System.out.println("Got REQUEST:\n" + request);
    }

    @Override
    protected void doBye(SipServletRequest request) {
        System.out.println("Got BYE:\n" + request);
        super.doBye(request);
    }

    @Override
    protected void doResponse(SipServletResponse response) {
        System.out.println("Got RESPONSE:\n" + response);
    }

    /** SIP error listeners. */
    @Override
    public void noAckReceived(SipErrorEvent e) {
    }

    @Override
    public void noPrackReceived(SipErrorEvent e) {
    }

}