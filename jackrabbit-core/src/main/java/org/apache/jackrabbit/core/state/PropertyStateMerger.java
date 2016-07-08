package org.apache.jackrabbit.core.state;

import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.spi.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merge properties
 */
public class PropertyStateMerger {

    /** Logger instance */
    private static final Logger log =
            LoggerFactory.getLogger(PropertyStateMerger.class);

    private static Map<Name, PropertyStateMergerAlgorithm> mergers = new ConcurrentHashMap<Name, PropertyStateMergerAlgorithm>();

    public static void registerMerger(Name name, PropertyStateMergerAlgorithm merger) {
        mergers.put(name, merger);
    }

    public static boolean merge(PropertyState propertyState, MergeContext context) {
    	
        PropertyState overlayedState = (PropertyState) propertyState.getOverlayedState();
        if (overlayedState == null
                || propertyState.getModCount() == overlayedState.getModCount()) {
            return false;
        }
        
        boolean merged = false;

        PropertyStateMergerAlgorithm merger = mergers.get(propertyState.getName());
        if (merger != null) {
            merged = merger.merge(propertyState, context);
        }

        return merged;
    }

    public interface MergeContext {
        PropertyState getPropertyState(PropertyId id) throws ItemStateException;
    }

    public interface PropertyStateMergerAlgorithm {
        boolean merge(PropertyState propertyState, MergeContext context);
    }

    /**
     * Takes the new value, whatever was in the overlayed modified state
     */
    public static class OverrideValueMergerAlgorithm implements PropertyStateMergerAlgorithm {
        public boolean merge(PropertyState propertyState, MergeContext context) {
        	if (log.isDebugEnabled()) {
                log.debug("{} : ignore persisted change, keep : {}", propertyState.getName(), Arrays.asList(propertyState.getValues()));
        	}
            propertyState.setModCount(propertyState.getOverlayedState().getModCount());
            return true;
        }
    }

    /**
     * Takes the most recent value of the 2 property values, using node comparison based on the date property passed in constructor.
     * If no date property specified, directly compare the property values
     */
    public static class MostRecentDateValueMergerAlgorithm extends OverrideValueMergerAlgorithm implements PropertyStateMergerAlgorithm {
        private Name baseDateProperty;

        public MostRecentDateValueMergerAlgorithm(Name baseDateProprty) {
            this.baseDateProperty = baseDateProprty;
        }

        public boolean merge(PropertyState propertyState, MergeContext context) {
            try {
                PropertyState datePropertyState = baseDateProperty != null ? context.getPropertyState(new PropertyId(propertyState.getParentId(), baseDateProperty)) : propertyState;
                PropertyState dateOverlayedState = (PropertyState) datePropertyState.getOverlayedState();

                if (dateOverlayedState != null && !datePropertyState.isMultiValued() && datePropertyState.getType() == PropertyType.DATE &&
                        !dateOverlayedState.isMultiValued() && dateOverlayedState.getType() == PropertyType.DATE) {
                    try {
                        PropertyState overlayedState = (PropertyState) propertyState.getOverlayedState();

                        if (!dateOverlayedState.getValues()[0].getDate().before(datePropertyState.getValues()[0].getDate())) {
                        	if (log.isDebugEnabled()) {
                                log.debug("Persisted values for " + propertyState.getName() + " is more recent, copy value from there : " + Arrays.asList(propertyState.getValues()) + " / " + Arrays.asList(overlayedState.getValues()));
                        	}
                            propertyState.setValues(overlayedState.getValues());                        	
                        } else {
                        	if (log.isDebugEnabled()) {
                                log.debug(propertyState.getName() + " value seems to be more recent than persisted value, skip conflict and override : " + Arrays.asList(propertyState.getValues()) + " / " + Arrays.asList(overlayedState.getValues()));
                        	}
                        }
                        propertyState.setModCount(overlayedState.getModCount());
                        return true;
                    } catch (RepositoryException e) {
                        log.error("Cannot get date values", e);
                    }
                } else if (dateOverlayedState == null) {
                    // Date already merged or not changed, ignore
                    return super.merge(propertyState, context);
                }
            } catch (ItemStateException e) {
                log.error("Cannot get comparison date", e);
            }
            return false;
        }
    }

}
