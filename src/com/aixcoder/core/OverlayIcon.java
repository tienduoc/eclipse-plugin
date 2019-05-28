package com.aixcoder.core;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.internal.util.Util;

/**
 * An OverlayIcon consists of a main icon and an overlay icon
 */
@SuppressWarnings("restriction")
public class OverlayIcon extends CompositeImageDescriptor {

    // the size of the OverlayIcon
    private Point fSize = null;

    // the main image
    private ImageData fBase = null;

    // the additional image (a pin for example)
    private ImageData fOverlay = null;

    /**
     * @param base the main image
     * @param overlay the additional image (a pin for example)
     * @param size the size of the OverlayIcon
     */
    public OverlayIcon(ImageData base, ImageData overlay, Point size) {
        fBase = base;
        fOverlay = overlay;
        fSize = size;
    }

    @Override
	protected void drawCompositeImage(int width, int height) {
        final ImageData bg = fBase != null ? fBase : DEFAULT_IMAGE_DATA;
        drawImage(new ImageDataProvider() {
			@Override
			public ImageData getImageData(int zoom) {
				return bg;
			}
		}, 0, 0);

        if (fOverlay != null) {
			drawTopRight(fOverlay);
		}
    }

    /**
     * @param overlay the additional image (a pin for example)
     * to be drawn on top of the main image
     */
    protected void drawTopRight(ImageData overlay) {
        if (overlay == null) {
			return;
		}
        int x = getSize().x;
        final ImageData id = overlay;
        x -= id.width;
        drawImage(new ImageDataProvider() {
			@Override
			public ImageData getImageData(int zoom) {
				return id;
			}
		}, x, getSize().y - id.height);
    }

    @Override
	protected Point getSize() {
        return fSize;
    }

    @Override
	public int hashCode() {
        return Util.hashCode(fBase) * 17 + Util.hashCode(fOverlay);
    }

    @Override
	public boolean equals(Object obj) {
        if (!(obj instanceof OverlayIcon)) {
			return false;
		}
        OverlayIcon overlayIcon = (OverlayIcon) obj;
        return Util.equals(this.fBase, overlayIcon.fBase)
                && Util.equals(this.fOverlay, overlayIcon.fOverlay)
                && Util.equals(this.fSize, overlayIcon.fSize);
    }
}
