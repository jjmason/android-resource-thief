package jm.rt.service;
import jm.rt.service.ExportStatus;
interface IExportListener {
	void onExportStatusChanged(in String packageName, in int resourceId, in int status);
}