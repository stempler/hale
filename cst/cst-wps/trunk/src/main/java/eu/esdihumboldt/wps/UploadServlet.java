/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                  01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */

package eu.esdihumboldt.wps;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import eu.esdihumboldt.cst.transformer.service.CstFunctionFactory;

/**
 * 
 * @author jezekjan
 *
 */
public class UploadServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		processRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		processRequest(req, resp);
	}

	private void processRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			List<FileItem> items = upload.parseRequest(req);
			Iterator iter = items.iterator();
			File gmlFile = null;
			File omlFile = null;
			File schemaFile = null;
			File sourceschemaFile = null;
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();

				if (!item.isFormField()) {
					if (item.getFieldName().equals("gml")) {
						Date d = new Date();
						gmlFile = new File(this.getServletContext()
								.getRealPath("tmp")
								+ "/" + d.getTime() + "." + item.getFieldName());
						item.write(gmlFile);
					} else if (item.getFieldName().equals("oml")) {
						Date d = new Date();
						omlFile = new File(this.getServletContext()
								.getRealPath("tmp")
								+ "/" + d.getTime() + "." + item.getFieldName());
						item.write(omlFile);
					} else if (item.getFieldName().equals("schema")) {
						Date d = new Date();
						schemaFile = new File(this.getServletContext()
								.getRealPath("tmp")
								+ "/" + d.getTime() + "." + item.getFieldName());
						item.write(schemaFile);
					} else if (item.getFieldName().equals("sourceschema")) {
						Date d = new Date();
						sourceschemaFile = new File(this.getServletContext()
								.getRealPath("tmp")
								+ "/" + d.getTime() + "." + item.getFieldName());
						item.write(sourceschemaFile);
                                        }

				} else {
					//throw new IOException("unexpected field "
					//		+ item.getFieldName());
				}
			}

                        resp.setContentType("text/html");
                        String r = "<script type=\"text/javascript\">window.parent.handleResponse({success:true,";
                        
                        if (sourceschemaFile.length() > 0) {
                            r = r + "sourceschema: 'tmp/"+sourceschemaFile.getName()+"',";
                        }

                        r = r + "schema:'tmp/"+schemaFile.getName()+"',oml:'tmp/"+omlFile.getName()+"', gml:'tmp/"+gmlFile.getName()+"'})</script>";

			
			resp.getWriter().write(r);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		CstFunctionFactory.getInstance().registerCstPackage(
		"eu.esdihumboldt.cst.corefunctions");
	}

}
