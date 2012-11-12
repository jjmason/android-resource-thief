package jm.rt.service;
import jm.rt.service.ExportStatus;
import jm.rt.service.IStatusListener;
import jm.rt.service.ExportRequest;

interface IExportService {  
	int export(in ExportRequest request);
	ExportStatus getStatus(in int requestId);
	void addStatusListener(in IStatusListener listener);
	void removeStatusListener(in IStatusListener listener);
	void cancel(in int requestId);
	void removeFinished();
}