package jm.rt.res;

import java.util.HashMap;
import java.util.Map;

import jm.rt.R;
import jm.util.Args;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

public class Resource {
	public static enum Type {
		DRAWABLE, LAYOUT, COLOR, STRING, INTEGER, BOOL, DIMEN, MENU, STYLE, STYLEABLE, RAW, PLURALS, MIPMAP, INTERPOLATOR, ID, FRACTION, ATTR, ARRAY, ANIMATOR, ANIM, XML;

		public static final int XML_TYPES = buildMask(DRAWABLE, LAYOUT, COLOR,
				MENU, INTERPOLATOR, ANIMATOR, ANIM, XML);

		public static final int FILE_TYPES = buildMask(DRAWABLE, LAYOUT, COLOR,
				MENU, INTERPOLATOR, ANIM, ANIMATOR, XML, MIPMAP, RAW);

		public static final int VALUE_TYPES = buildMask(COLOR, STRING, INTEGER,
				BOOL, DIMEN, STYLE, STYLEABLE, PLURALS, ID, FRACTION, ATTR,
				ARRAY);

		public final int mask() {
			return (1 << ordinal());
		}

		public boolean match(int mask) {
			return 0 != (mask() & mask);
		}

		public boolean match(Type...types){
			return match(buildMask(types));
		}
		
		public static final int buildMask(Type... types) {
			int mask = 0;
			for (Type type : types)
				mask |= type.mask();
			return mask;
		}

		public static final Type fromName(String name) {
			return valueOf(name.toUpperCase());
		}

		public static final String maskToString(int mask) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Type type : values()) {
				if (type.match(mask)) {
					if (!first) {
						sb.append("|");
					} else {
						first = false;
					}
					sb.append(type);
				}
			}
			return sb.toString();
		}

		@Override
		public final String toString() {
			return name().toLowerCase();
		}
	}

	public static class ResourceException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ResourceException() {
		}

		public ResourceException(String message) {
			super(message);
		}

		public ResourceException(Throwable cause) {
			super(cause);
		}

		public ResourceException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	private static final Map<String, Integer> sExtensionToIcon = new HashMap<String, Integer>();
	static {
		// TODO initialize sExtensionToIcon
	}

	// TODO better icon for sUnknownFileTypeIcon
	private static final int sUnknownFileTypeIcon = R.drawable.ic_launcher;

	private static final Map<Type, Integer> sResourceTypeToIcon = new HashMap<Resource.Type, Integer>();
	static {
		// TODO initialize sResourceTypeToIcon
	}

	// TODO better icon for sUnknownResourceTypeIcon
	private static final int sUnknownResourceTypeIcon = R.drawable.ic_launcher;

	private static final String TAG = Resource.class.getSimpleName();

	public final int id;
	public final Resources resources;
	public final Context context;
	
	private Type mType;
	private String mEntryName;
	private String mTypeName;
	private String mPackageName;
	private String mFullName;
	private Drawable mDrawable;
	private Exception mError;
	private String mErrorMessage;
	private String mFileName;
	private Boolean mIsValid;
	private Integer mColor;
	
	
	public Resource(Context context, int id, Resources resources) {
		this.id = Args.notZero(id);
		this.context = Args.notNull(context);
		this.resources = Args.notNull(resources);
	}
	
	public Resource(Context context, int id, String packageName) 
			throws PackageManager.NameNotFoundException {
		this(context, id, context.getPackageManager().getResourcesForApplication(packageName));
	}
	
	public Resource(Context context, int id, ApplicationInfo applicationInfo) 
			throws PackageManager.NameNotFoundException {
		this(context, id, context.getPackageManager().getResourcesForApplication(applicationInfo));
	}
	
	public Resource(Context context, int id, PackageInfo packageInfo) 
			throws PackageManager.NameNotFoundException{
		this(context, id,packageInfo.applicationInfo);
	}

	/**
	 * Get the name of the resource. For example, R.id.foo has the name "foo".
	 * 
	 * @return the name of the resource
	 */
	public final String getName() {
		if (mEntryName == null) {
			mEntryName = resources.getResourceEntryName(id);
		}
		return mEntryName;
	}

	/**
	 * Get the full name of the resource, like
	 * <code>"com.example.mypackage:drawable/my_drawable"</code>.
	 * 
	 * <p>
	 * This value can be constructed as follows:
	 * </p>
	 * <code><pre>
	 * Resource res = ...; 
	 * String fullName = res.getPackageName() + ":" + res.getTypeName() + "/" + res.getName();
	 * </pre></code>
	 * 
	 * @return the full name of this resource
	 */
	public final String getFullName() {
		if (mFullName == null) {
			mFullName = resources.getResourceName(id);
		}
		return mFullName;
	}

	/**
	 * Get the type name for this resource, for example <code>"anim"</code>,
	 * <code>"drawable"</code>, or <code>"id"</code>.
	 * 
	 * @return the resource type name
	 */
	public String getTypeName() {
		if (mTypeName == null) {
			mTypeName = resources.getResourceTypeName(id);
		}
		return mTypeName;
	}

	/**
	 * The {@link Type} enumeration value for this resource.
	 * 
	 * @return the resource type
	 */
	public Type getType() {
		if (mType == null) {
			mType = Type.fromName(getTypeName());
		}
		return mType;
	}

	/**
	 * The package name for this resource.
	 * 
	 * @return the package name
	 */
	public String getPackageName() {
		if (mPackageName == null) {
			mPackageName = resources.getResourcePackageName(id);
		}
		return mPackageName;
	}

	/**
	 * If an error occurred while loading a drawable, color or file resource,
	 * it's here.
	 * 
	 * @return the exception thrown
	 */
	public Exception getError() {
		return mError;
	}

	/**
	 * If an exception was thrown while loading this resource, a brief
	 * description is stored in this property
	 * 
	 * @return a brief description of the error, such as
	 *         <code>"Error loading drawable"</code>.
	 */
	public String getErrorMessage() {
		return mErrorMessage;
	}

	/**
	 * Load a {@link Drawable} for this resource. The behavior of this method
	 * depends on the value returned by {@link #getType()}
	 * 
	 * @param isIcon
	 *            currently unused. Pass false for future compatibility.
	 * @return a {@link Drawable} representing this resource.
	 */
	public Drawable getDrawable(boolean isIcon) {
		if (mDrawable != null)
			return mDrawable;

		switch (getType()) {
		case DRAWABLE:
			mDrawable = safeGetDrawableResource();
			break;
		case COLOR:
			mDrawable = safeGetColorDrawable();
			break;
		case MIPMAP:
			mDrawable = safeGetMipMapDrawable();
			break;
		case RAW:
			mDrawable = safeGetDrawableForFileType();
			break;
		default:
			mDrawable = getDrawableForResourceType();
			break;
		}
		
		return mDrawable;
	}

	public Drawable getDrawable() {
		return getDrawable(false);
	}

	
	public boolean isValid(){
		if(mIsValid != null)
			return mIsValid;
		try{
			getType();
			mIsValid = true;
		}catch(Exception e){
			handleError(e, "Unable to resolve resource type");
			mIsValid = false;
		}
		return mIsValid;
	}
	
	public boolean hasColor(){
		if(getType() != Type.COLOR)
			return false;
		if(mColor == null){
			try{
				mColor = getColorNoCheck();
			}catch(Exception e){
				mColor = null;
				handleError(e, "Error getting color");
			}
		}
		return mColor != null;
	}
	
	public int getColor(){
		return hasColor() ? mColor : null;
	}
	
	private int getColorNoCheck(){
		return resources.getColor(id);
	}
	
	public boolean hasFile() {
		if (mFileName != null)
			return true;
		if (!getType().match(Type.FILE_TYPES))
			return false;
		try {
			return getFileNameNoCheck() != null;
		} catch (Exception e) {
			return false;
		}
	}

	public String getFileName() {
		if (hasFile()) {
			if (mFileName == null) {
				try {
					mFileName = getFileNameNoCheck();
				} catch (Exception e) {
					handleError(e, "Error getting file");
				}
			}
		}
		return mFileName;
	}

	private String getFileNameNoCheck() {
		TypedValue outValue = new TypedValue();
		resources.getValue(id, outValue, true);
		if(outValue.type == TypedValue.TYPE_STRING
				&& outValue.string != null){
			String file = outValue.string.toString();
			// TODO see if it exists!
			return file;
		}
		throw new ResourceException(String.format("resource 0x%08X is not a filename (type=0x%08X)", 
				id, outValue.type));
	}

	private Drawable safeGetDrawableResource() {
		try {
			return getDrawableResource();
		} catch (Exception e) {
			handleError(e, "Error loading drawable");
			return null;
		}
	}

	private Drawable getDrawableResource() throws Exception {
		return resources.getDrawable(id);
	}

	private Drawable safeGetColorDrawable() {
		try {
			return getColorDrawable();
		} catch (Exception e) {
			handleError(e, "Error loading color");
			return null;
		}
	}

	private void handleError(Exception error, String message) {
		Log.e(TAG, String.format("%s (id=0x%08X)", message, id), error);
		mError = error;
		mErrorMessage = message;
	}

	private Drawable getColorDrawable() throws Exception {
		return new ColorDrawable(resources.getColor(id));
	}

	private Drawable safeGetMipMapDrawable() {
		try {
			return getMipMapDrawable();
		} catch (Exception e) {
			handleError(e, "Error gettgetGenericDrawable();ing mipmap");
			return null;
		}
	}

	private Drawable getMipMapDrawable() {
		return resources.getDrawable(id);
	}

	private Drawable getDrawableForFileType(){
		Integer resId = null;
		String fileName = getFileName();
		if(fileName != null){
			String[] parts = fileName.split("\\.");
			resId = sExtensionToIcon.get(parts[parts.length - 1]);
		}
		if(resId == null)
			resId = sUnknownFileTypeIcon;
		return context.getResources().getDrawable(resId);
	}

	private Drawable safeGetDrawableForFileType(){
		try{
			return getDrawableForFileType();
		}catch(Exception e){
			handleError(e, "Error getting drawable for file type");
			return null;
		}
	}
	
	public Drawable getDrawableForResourceType(){
		Integer resId = sResourceTypeToIcon.get(getType());
		if(resId == null){
			resId = sUnknownResourceTypeIcon;
		}
		return context.getResources().getDrawable(resId);
	}
	
	@Override
	public String toString() {
		return getFullName();
	}
}
