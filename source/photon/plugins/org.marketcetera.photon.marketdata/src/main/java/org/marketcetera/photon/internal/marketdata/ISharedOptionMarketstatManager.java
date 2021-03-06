package org.marketcetera.photon.internal.marketdata;

import java.util.Map;

import org.marketcetera.photon.model.marketdata.impl.MDMarketstatImpl;
import org.marketcetera.trade.Option;
import org.marketcetera.util.misc.ClassVersion;

import com.google.inject.ImplementedBy;

/* $License$ */

/**
 * Interface for a manger of latest tick market data for an entire option chain
 * that shares a common underlying equity.
 * 
 * @author <a href="mailto:will@marketcetera.com">Will Horn</a>
 * @version $Id$
 * @since 2.0.0
 */
@ClassVersion("$Id$")
@ImplementedBy(SharedOptionMarketstatManager.class)
public interface ISharedOptionMarketstatManager extends
        IDataFlowManager<Map<Option, MDMarketstatImpl>, SharedOptionMarketstatKey> {
}
