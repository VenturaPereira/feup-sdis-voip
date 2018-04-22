import javax.servlet.*;
import javax.servlet.sip.*;

public class ProxyServlet extends SipServlet implements SipErrorListener, Servlet {

    /** Creates a new instance of ProxyServlet. */
    public ProxyServlet() {}

    /** SIP error listeners. */
    @Override
    public void noAckReceived(SipErrorEvent e) {
    }

    @Override
    public void noPrackReceived(SipErrorEvent e) {
    }

}