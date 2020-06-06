import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class IndexServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("init");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("service start");
        super.service(req, resp);
        System.out.println("service finish");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        PrintWriter w = null;
        try {
            w = resp.getWriter();
            PrintWriter writer = w;
            System.out.println(req.getParameterMap().getClass().getCanonicalName());
            req.getParameterMap().forEach((key, values)->{
                Arrays.stream(values).forEach(
                        s -> writer.printf("%s = %s\n", key, s)
                );
            });
            // System.out.println("doGet");
        } finally {
            w.close();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        System.out.println("destroy");
    }
}
