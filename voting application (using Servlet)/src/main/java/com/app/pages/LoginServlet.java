package com.app.pages;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.app.dao.UserDaoImpl;
import com.app.entities.User;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet(value = "/login", loadOnStartup = 1) // eager init
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDaoImpl userDao;
//	overriding form of the method CANT throw any NEW or 
	//BROADER checked excs 

	@Override
	public void init() throws ServletException {
		System.out.println("in init of " + getClass());
		try {
			// create user dao instance
			//deprndenr:servlet,dependency -user Dao
			//depenedent object its create its own dependency
			userDao = new UserDaoImpl();
		} catch (Exception e) {
			// centralized exc handling in Servlet
			/*
			 * In case of err in init --To inform the WC throw ServletException --WC will
			 * abort the life cycle Ctor of javax.servlet.ServletException(String mesg,
			 * Throwable rootCause)
			 */
		throw new ServletException("err in init - " + getClass(), e);
		}
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		try {
			// WC invokes it once @ end of the life cycle
			// clean up Dao
			userDao.cleanUp();
		} catch (Exception e) {
			System.out.println("err in destroy - " + getClass());
		}

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// 1. set resp cont type
		response.setContentType("text/html");
		// 2. Get PW to send text resp
		try (PrintWriter pw = response.getWriter()) {
			// 3. Get req params(email n pwd) from the req
			String email = request.getParameter("em");
			String password = request.getParameter("pass");
			// 4. Invoke user dao's --sign in method for user authentication
			User user = userDao.signIn(email, password);
			// 5 . check if valid (via null)
			if (user == null) {
				// invalid login --send retry link --login.html
				pw.print("<h5>Invalid Login , Please  <a href='login.html'>Retry</a><h5>");
			} else {
				// valid login -->create a cookie
				//javax.servlet.http.Cookie(String cookieName,String,cookieVal)
				Cookie c1=new Cookie("user_details",user.toString());
				//2.send the Cookie to client ,using resp header
				//HttpServletResponse's method
				//public void addCookie(Cookie c)
				response.addCookie(c1);
//			//contiune to role based authorization
				if (user.getRole().equals("voter")) {
					//=> voter login --> check the voting status
					if(user.isStatus()) //=> already voted --> redirect to logout page
						response.sendRedirect("logout");
					else //voter : not yet voted --> redirect to candidate list page
						response.sendRedirect("candidate_list");	
					
					/*
					 * WC-1.clears /empties the PW s buffer
					 * 2.send temp redirect resp-SC 302,Location="candidate"
					 * Set-Cookie-cookiename-value 
					 * resp body-empty
					 * 3.clint browser-chks privacy seeting 
					 * cookies bloked--wont wont be store--cant remember the client
					 * accepted--cookie age --def value -1=>saves it in cache(cookie storage)
					 * 
					 * 4.clint browser -send NEXT req (redirect)
					 * URL-http://host:port/day2.1/candidate_list
					 * method -GET-->CAndidateList servlet
					 * +add  the cookie in req header
				     *
					 */

				} else {
					// admin login -- redirect the clnt to admin page in NEXT request coming from
					// the clnt
					response.sendRedirect("admin");
				}

			}

		} // JVM : pw.close --> flush --> render/commits the resp
		catch (Exception e) {
			// inform the WC about the exc
			throw new ServletException("err in servicing " + getClass(), e);
		}

	}

}
