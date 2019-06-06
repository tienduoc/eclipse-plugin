package com.aixcoder.extension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.aixcoder.core.OverlayIcon;
import com.aixcoder.utils.Predict.SortResult;

/**
 * Sort instances of {@link AiXCompletionProposal} to the top. p1 < p2 means p1
 * is before p2.
 */
public class AiXSorter implements ICompletionProposalSorter {

	static final Image image = Activator
			.imageDescriptorFromPlugin(Activator.getDefault().getBundle().getSymbolicName(), "icons/aix_sort.png")
			.createImage();

	private ICompletionProposalSorter sorter;
	public SortResult[] list;

	public AiXSorter(ICompletionProposalSorter sorter) {
		this.sorter = sorter;
	}

	HashMap<String, Method> fImageMethodCache = new HashMap<String, Method>();

	Method _getSetImageMethod(ICompletionProposal p, Class<?> clz) {
		try {
			Method fImageMethod = clz.getDeclaredMethod("setImage", Image.class);
			fImageMethod.setAccessible(true);
			return fImageMethod;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			Class<?> superClz = clz.getSuperclass();
			if (superClz != null) {
				return getSetImageMethod(p, superClz);
			}
		}
		return null;
	}

	Method getSetImageMethod(ICompletionProposal p, Class<?> clz) {
		String name = clz.getName();
		if (!fImageMethodCache.containsKey(name)) {
			fImageMethodCache.put(clz.getName(), _getSetImageMethod(p, clz));
		}
		return fImageMethodCache.get(clz.getName());
	}

	HashMap<String, Field> fImageFieldCache = new HashMap<String, Field>();

	public ICompletionProposal longProposal;

	Field _getFImageField(ICompletionProposal p, Class<?> clz) {
		try {
			Field fImageField = clz.getDeclaredField("fImage");
			fImageField.setAccessible(true);
			return fImageField;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			Class<?> superClz = clz.getSuperclass();
			if (superClz != null) {
				return getFImageField(p, superClz);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}

	Field getFImageField(ICompletionProposal p, Class<?> clz) {
		String name = clz.getName();
		if (!fImageFieldCache.containsKey(name)) {
			fImageFieldCache.put(clz.getName(), _getFImageField(p, clz));
		}
		return fImageFieldCache.get(clz.getName());
	}

	static HashMap<Image, Image> cachedOverlays = new HashMap<Image, Image>();
	static Image blankImage = new Image(Display.getDefault(), 16, 16);

	double getScore(ICompletionProposal p, String s) {
		if (list != null && !(p instanceof AiXCompletionProposal)) {
			if (p instanceof AiXForcedSortCompletionProposal) {
				addImageOverlay(p);
				return ((AiXForcedSortCompletionProposal)p).getScore();
			}
			for (SortResult pair : list) {
				if (s.equals(pair.word) || s.startsWith(pair.word + " ") || s.startsWith(pair.word + "(")) {
					addImageOverlay(p);
					return pair.prob;
				}
			}
		}
		return 0;
	}

	private void addImageOverlay(ICompletionProposal p) {
		Image i = p.getImage();
		if (i == null) {
			i = blankImage;
		}
		if (!cachedOverlays.containsKey(i)) {
			Image overlay;
			OverlayIcon resultIcon = new OverlayIcon(i.getImageData(), image.getImageData(),
					new Point(16, 16));
			overlay = resultIcon.createImage();
			cachedOverlays.put(i, overlay);
		}
		Image overlay = cachedOverlays.get(i);
		setImage(p, overlay);
	}

	private void setImage(ICompletionProposal p, Image overlay) {
		boolean imageSet = false;
		Method setImageMethod = getSetImageMethod(p, p.getClass());
		if (setImageMethod != null) {
			try {
				setImageMethod.invoke(p, overlay);
				imageSet = true;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if (!imageSet) {
			Field fImageField = getFImageField(p, p.getClass());
			imageSet = true;
			if (fImageField != null) {
				try {
					fImageField.set(p, overlay);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	double getScore(ICompletionProposal p) {
		if (longProposal == p && p instanceof AiXCompletionProposal) {
			setImage(p, ProposalFactory.image);
			return 1;
		}
		if (p instanceof AiXCompletionProposal) {
			return longProposal == null ? 1 : 0;
		}
		return getScore(p, p.getDisplayString());
	}

	@Override
	public int compare(ICompletionProposal p1, ICompletionProposal p2) {
		double scoreDiff = getScore(p1) - getScore(p2);
		if (scoreDiff > 0) {
			return -1;
		} else if (scoreDiff == 0) {
			return sorter == null ? 0 : sorter.compare(p1, p2);
		} else {
			return 1;
		}
	}

}