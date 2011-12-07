/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2011, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.project.internal.property;

import java.net.URL;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.objectproperty.ObjectPropertyCatalogListener;
import net.refractions.udig.project.BlackboardEvent;
import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.IBlackboardListener;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.ProjectBlackboardConstants;
import net.refractions.udig.ui.operations.AbstractPropertyValue;
import net.refractions.udig.ui.operations.PropertyValue;

import org.geotools.data.FeatureSource;

/**
 * Returns true if the layer has a FeatureSource as a resource.
 * 
 * @author Jody Garnett (LISAsoft)
 * @since 1.3.0
 */
public class FeatureSourceResourceProperty extends AbstractPropertyValue<ILayer>
        implements PropertyValue<ILayer> {

    private volatile AtomicBoolean isEvaluating = new AtomicBoolean(false);
    private Set<URL> ids = new CopyOnWriteArraySet<URL>();

    public boolean canCacheResult() {
        return false;
    }

    public boolean isBlocking() {
        return false;
    }

    public boolean isTrue( final ILayer object, String value ) {
        isEvaluating.set(true);
        try {
            object.getBlackboard().addListener(new IBlackboardListener(){
                public void blackBoardChanged( BlackboardEvent event ) {
                    if (event.getKey().equals(ProjectBlackboardConstants.LAYER__DATA_QUERY)) {
                        notifyListeners(object);
                    }
                }
                public void blackBoardCleared( IBlackboard source ) {
                    notifyListeners(object);
                }
            });

            final IGeoResource resource = object.findGeoResource(FeatureSource.class);
            if (resource != null && ids.add(resource.getIdentifier())) {
                CatalogPlugin.getDefault().getLocalCatalog().addCatalogListener(
                        new ObjectPropertyCatalogListener(object, resource, isEvaluating, this));
            }
            boolean canResolve = resource.canResolve(FeatureSource.class);
            return canResolve;
        } catch (Exception e) {
            return false;
        } finally {
            isEvaluating.set(false);
        }
    }

}