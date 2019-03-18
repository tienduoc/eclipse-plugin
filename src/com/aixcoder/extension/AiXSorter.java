package com.aixcoder.extension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalSorter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.aixcoder.core.OverlayIcon;
import com.aixcoder.utils.Pair;

/**
 * Sort instances of {@link AiXCompletionProposal} to the top. p1 < p2 means p1
 * is before p2.
 */
public class AiXSorter implements ICompletionProposalSorter {

	static final Image image = Activator
			.imageDescriptorFromPlugin(Activator.getDefault().getBundle().getSymbolicName(), "icons/aix_sort.png")
			.createImage();

	private ICompletionProposalSorter sorter;
	public List<Pair<Double, String>> list;

	public AiXSorter(ICompletionProposalSorter sorter) {
		this.sorter = sorter;
	}

	Method getSetImageMethod(ICompletionProposal p, Class<?> clz) {
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

	Field getFImageField(ICompletionProposal p, Class<?> clz) {
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

	double getScore(ICompletionProposal p, String s) {
		if (list != null) {
			for (Pair<Double, String> pair : list) {
				if (s.equals(pair.second) || s.startsWith(pair.second + " ") || s.startsWith(pair.second + "(")) {
					Image i = p.getImage();
					if (i != null) {
						OverlayIcon resultIcon = new OverlayIcon(i.getImageData(), image.getImageData(),
								new Point(16, 16));
						i = resultIcon.createImage();
					}
					boolean imageSet = false;
					Method setImageMethod = getSetImageMethod(p, p.getClass());
					if (setImageMethod != null) {
						try {
							setImageMethod.invoke(p, i);
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
								fImageField.set(p, i);
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
					return pair.first;
				}
			}
		}
		return 0;
	}

	double getScore(ICompletionProposal p) {
		if (p instanceof AiXCompletionProposal) {
			return 1;
		}
		return getScore(p, p.getDisplayString());
	}

	@Override
	public int compare(ICompletionProposal p1, ICompletionProposal p2) {
		double scoreDiff = getScore(p1) - getScore(p2);
		if (scoreDiff > 0) {
			return -1;
		} else if (scoreDiff == 0) {
			return sorter.compare(p1, p2);
		} else {
			return 1;
		}
	}

}