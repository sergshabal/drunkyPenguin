package net.groster.moex.forts.drunkypenguin.core.fast.config.xml;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.NONE)
public class MarketDataGroup {

    @XmlAttribute
    private FeedType feedType;
    @XmlAttribute
    private MarketID marketID;
    @XmlAttribute
    private MarketDepth marketDepth;
    @XmlAttribute
    private String label;
    @XmlElementWrapper
    @XmlElement(name = "connection")
    private List<Connection> connections;

    public FeedType getFeedType() {
        return feedType;
    }

    public MarketID getMarketID() {
        return marketID;
    }

    public List<Connection> getConnections() {
        return connections;
    }

}
