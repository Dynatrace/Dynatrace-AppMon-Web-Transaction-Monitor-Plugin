package wt.listener;

import java.io.IOException;

import wt.measure.IMeasureCenter;
import wt.measure.IMeasureCenterKeeper;
import wt.measure.MeasureCenter;
import wt.measure.MeasureValue;

import com.dynatrace.diagnostics.global.Constants;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 *
 * <p></p>
 *
 * @author ardeshir.arfaian
 * @date 17.07.2008
 *
 */
public class WTWebConnectionWrapper implements WebConnection, IMeasureCenterKeeper {

	/** */
    private final WebConnection wrappedWebConnection_;

    /** */
    private IMeasureCenter measureCenter;

    /**
     * Constructs a WebConnection object wrapping provided WebConnection.
     * @param webConnection the webConnection that does the real work
     * @throws IllegalArgumentException if the connection is <code>null</code>
     */
    public WTWebConnectionWrapper(final WebConnection webConnection) throws IllegalArgumentException {
        if (webConnection == null) {
            throw new IllegalArgumentException("Wrapped connection can't be null");
        }
        wrappedWebConnection_ = webConnection;
    }

    /**
     * Constructs a WebConnection object wrapping the connection of the WebClient and places itself as
     * connection of the WebClient.
     *
     * @param webClient the WebClient which WebConnection should be wrapped
     * @throws IllegalArgumentException if the WebClient is <code>null</code>
     */
    public WTWebConnectionWrapper(final WebClient webClient) throws IllegalArgumentException {
        if (webClient == null) {
            throw new IllegalArgumentException("WebClient can't be null");
        }
        wrappedWebConnection_ = webClient.getWebConnection();
        webClient.setWebConnection(this);
    }

    /**
     * {@inheritDoc}
     * The default behavior of this method is to return getResponse() on the wrapped connection object.
     * Additionally this method measures request &amp; response bytes, load time and number of requests.
     */
    public WebResponse getResponse(final WebRequestSettings webRequestSettings) throws IOException {
    	StringBuilder dtHeader = null;

    	if(measureCenter.isDtdTaggingEnabled()){

    		dtHeader = new StringBuilder();

    		//set virtual user
    		dtHeader.append("VU=");
    		dtHeader.append(measureCenter.getTransactionLocation());

    		//set page context
			dtHeader.append(";PC=");
			dtHeader.append(measureCenter.getMeasureValue(MeasureCenter.KEY_NUM_OF_REQUESTS).getIntValue());

			//set transaction id
			dtHeader.append(";ID=");
			dtHeader.append(measureCenter.getTransactionId());

			//set transaction & timer name
			dtHeader.append(";NA=");
			dtHeader.append(measureCenter.getTransactionName());
			dtHeader.append("-");
			dtHeader.append(measureCenter.getTimerName());

    		webRequestSettings.addAdditionalHeader(Constants.HEADER_DYNATRACE, dtHeader.toString());
    		webRequestSettings.addAdditionalHeader("transactionLocation", measureCenter.getTransactionLocation());
    		webRequestSettings.addAdditionalHeader("transactionStep", "" + measureCenter.getMeasureValue(MeasureCenter.KEY_NUM_OF_REQUESTS).getIntValue());
    		webRequestSettings.addAdditionalHeader("transactionName", measureCenter.getTransactionName());
	    	webRequestSettings.addAdditionalHeader("transactionId", "" + measureCenter.getTransactionId());
	    	webRequestSettings.addAdditionalHeader("timername", measureCenter.getTimerName());
		}

        WebResponse response = wrappedWebConnection_.getResponse(webRequestSettings);

        long requestBytes = MeasureCenter.countRequestBytes(webRequestSettings);
        long responseBytes = MeasureCenter.countResponseBytes(response);

    	measureCenter.increaseMeasureValue(MeasureCenter.KEY_REQUEST_BYTES, new MeasureValue(requestBytes));
    	measureCenter.increaseMeasureValue(MeasureCenter.KEY_RESPONSE_BYTES, new MeasureValue(responseBytes));
    	measureCenter.increaseMeasureValue(MeasureCenter.KEY_NUM_OF_REQUESTS, new MeasureValue(1));
    	measureCenter.increaseMeasureValue(MeasureCenter.KEY_AGGREGATED_RTT, new MeasureValue(response.getLoadTime()));


    	return response;
    }

	@Override
	public IMeasureCenter getIMeasureCenter() {
		return measureCenter;
	}

	@Override
	public void setIMeasureCenter(IMeasureCenter measureCenter) {
		this.measureCenter = measureCenter;
	}

}
