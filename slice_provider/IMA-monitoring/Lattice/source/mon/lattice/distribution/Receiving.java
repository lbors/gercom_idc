// Receiving.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2008

package mon.lattice.distribution;

import mon.lattice.core.TypeException;
import java.io.IOException;
import java.io.ByteArrayInputStream;

/**
 * An interface for distribution components that need
 * to do receiving from the transport.
 */
public interface Receiving {
    /**
     * This method is called just after a message
     * has been received from some underlying transport
     * at a particular multicast address.
     */
    public void received(ByteArrayInputStream bis, MetaData metaData) throws IOException, TypeException, ReflectiveOperationException;

    /**
     * This method is called just after there has been an error
     * in received from some underlying transport.
     * This passes the exception into the Receiving object.
     */
    public void error(Exception e);

    /**
     * This method is called just after there has been EOF
     * in received from some underlying transport.
     */
    public void eof();



}
