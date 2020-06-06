import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"second-servlet"})
@MultipartConfig(location = "/home/yurii/IdeaProjects/ItStep/OdE2191/javaee/servlet")
public class SecondServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        for (Part part : req.getParts()) {
            Model model = new Model();
            if (part.getName().equals("user_name")) {
                InputStream inputStream = part.getInputStream();
                InputStreamReader isr = new InputStreamReader(inputStream);
                String userName = new BufferedReader(isr)
                        .lines()
                        .collect(Collectors.joining("\n"));
                log(userName);
                model.userName = userName;
            } else if(part.getName().equals("image")) {
                model.image = UUID.randomUUID().toString() + part.getSubmittedFileName();
                part.write(model.image);
            }
            Storage.modelList.add(model);
        }
        resp.sendRedirect("/single-servlet");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // http://0.0.0.0:8888/single-servlet/second-servlet?action=getFileNames
        // Получить список имен файлов, ранее сохраненных на сервере
        if (req.getParameter("action").equals("getFileNames")){
            Storage.modelList.forEach(model -> {
                try {
                    // resp.getWriter().println(model.image);
                    if (model.image != null){
                        resp.getWriter().println("<a href='http://0.0.0.0:8888/single-servlet/second-servlet?action=getFile&filename="+model.image+"'>"+model.image+"</a><br>");
                        // resp.getWriter().println("<a href='http://0.0.0.0:8888/single-servlet/second-servlet?action=getInfo&username="+model.userName+"'>"+model.userName+"</a><br>");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (req.getParameter("action").equals("getFile")) {
            // http://0.0.0.0:8888/single-servlet/second-servlet?action=getFile&filename=file_name
            // Получить файл по его имени, переданному от клиента (вызываем кликом по гиперссылке
            // на странице со списком доступных файлов)
            resp.setContentType("image/jpeg");
            ServletOutputStream out = resp.getOutputStream();
            FileInputStream fin =
                    new FileInputStream("/home/yurii/IdeaProjects/ItStep/OdE2191/javaee/servlet/" + req.getParameter("filename"));
            BufferedInputStream bin = new BufferedInputStream(fin);
            BufferedOutputStream bout = new BufferedOutputStream(out);
            int ch = 0;
            while((ch = bin.read()) != -1)
            {
                bout.write(ch);
            }
            bin.close();
            fin.close();
            bout.close();
            out.close();
        }
        // TODO здесь добавить еще один if, который будет срабатывать на запрос на адрес вида:
        // http://0.0.0.0:8888/single-servlet/second-servlet?action=getInfo&username=user_name,
        // где user_name - динамически подставленное имя файла
        // по имени пользователя найдите из списка моделей одну модель,
        // сформируйте при помощи вызовов метода writer.println(...)
        // веб-страницу для клиента, в разметку которой добавьте элементы:
        // h2 - с именем пользователя
        // и img со ссылкой на изображение, подставленной в атрибут src
        // (имя пользователя и имя файла получите из модели)
        // подсказка: в атрибутах src формируйте гиперссылки вида:
        // http://0.0.0.0:8888/single-servlet/second-servlet?action=getFile&filename=file_name
        // , тогда браузер будет для получения каждого изображения сам обращаться к
        // текущему сервлету, к ветке логики метода doGet, которая возвращает картинку по ее имени
    }
}
