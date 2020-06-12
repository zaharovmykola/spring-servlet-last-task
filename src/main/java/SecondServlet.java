import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.swing.text.html.Option;
import java.io.*;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"second-servlet"})
@MultipartConfig(location = "D:\\IdeaProjects\\java-servlet-maven-master\\")
public class SecondServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // достаем части данных из сервлета и перебираем циклом
        Model model = new Model();
        for (Part part : req.getParts()) {
            // Іf - это что делать с каждой из частей данных
            // которые пришлт от клиента

            // если встречаем часть под именем user_name, то
            if (part.getName().equals("user_name")) {
                // InputStream - принимаем байты
                InputStream inputStream = part.getInputStream();
                // InputStreamReader - чтобы собрать данные встроку
                InputStreamReader isr = new InputStreamReader(inputStream);
                // обрабатываем многострочность с помощью BufferedReader
                // и сохраням их через стрим в переменную String
                String userName = new BufferedReader(isr)
                        .lines()
                        .collect(Collectors.joining("\n"));
                // выводим на сервер инфо
                log(userName);
                // сохраняем userName в поле обьекта
                model.userName = userName;
                // если встречаем часть под именем image, то
            } else if (part.getName().equals("image")) {
                // сохраняем название картинки в поле обьекта
                model.image = UUID.randomUUID().toString() + part.getSubmittedFileName();
                // сохраняем на жесткий дисл под именем, которое состоит из
                // случайной части и реального имени файла
                part.write(model.image);
            }
            // и сохраням обьект в лист
        }
        Storage.modelList.add(model);
        // Что после всего этого отправить клиенту - указания браузеру еще куда-то обратится

        // в ответ присылаем браузеру указания перейти на заданный адрес
        // и браузер осуществляет этот переход
        resp.sendRedirect("/single-servlet");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // http://0.0.0.0:8888/single-servlet/second-servlet?action=getFileNames
        // Получить список имен файлов, ранее сохраненных на сервере
        if (req.getParameter("action").equals("getFileNames")) {
            // перебираем все елементы листа

            resp.getWriter().println(
                    "<!DOCTYPE html>\n" +
                            "<html lang=\"en\">\n" +
                            "<head>\n" +
                            "    <meta charset=\"UTF-8\">\n" +
                            "    <title>Dream job</title>\n" +
                            "<style>"+
                            ".fig {" +
                            "text-align: center;" +
                            "}" +
                            "</style>" +
                            "</head>\n" +
                            "<body>\n" +
                            "<div class='fig'>" +
                            "<a href='http://127.0.0.1:8888/single-servlet'>Add Files</a><br><br>" +
                            "<p></p>" +
                            "</div>" +
                            "</body>\n" +
                            "</html>"
            );

            Storage.modelList.forEach(model -> {
                try {

                    if (model.image != null) {

                        resp.getWriter().println(
                                "<!DOCTYPE html>\n" +
                                        "<html lang=\"en\">\n" +
                                        "<head>\n" +
                                        "    <meta charset=\"UTF-8\">\n" +
                                        "    <title>Dream job</title>\n" +
                                        "<style>"+
                                        ".fig {" +
                                        "text-align: center;" +
                                        "}" +
                                        "</style>" +
                                        "</head>\n" +
                                        "<body>\n" +
                                        "<div class='fig'>" +
                                        "<a href='http://127.0.0.1:8888/single-servlet/second-servlet?action=getInfo&username=" + model.userName + "'>" + model.userName + "</a><br><br>" +
                                        "<p></p>" +
                                        "</div>" +
                                        "</body>\n" +
                                        "</html>"
                        );

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            // Если к нам придет гет-запрос у которого параметр action равний getFile, то мы
        } else if (req.getParameter("action").equals("getFile")) {

            // http://0.0.0.0:8888/single-servlet/second-servlet?action=getFile&filename=file_name
            // Получить файл по его имени, переданному от клиента (вызываем кликом по гиперссылке
            // на странице со списком доступных файлов)
            // устанавливаем тип данных, который вернем клиенту
            // что за тип данных сейчас получит браузер
            resp.setContentType("image/jpeg"); // - подготавливаемся к выдаче картинки
            // через эту переменную OutputStream высылаем клиенту байты и картинки
            // закачает файлик с сервера, передаем в ServletOutputStream,
            // а ServletOutputStream выбрасывает эти данные на веб-клиента - веб-браузер
            //ServletOutputStream - возвращает данные клиенту
            ServletOutputStream out = resp.getOutputStream(); // - подготавливаемся ее вытолкнуть через поток
            // FileInputStream - закачываем внутрь логики сервлета файл с жесткого диска сервера
            // FileInputStream - подготавливает эти данные из файла, вот их выкачивать
            FileInputStream fin =
                    new FileInputStream("D:\\IdeaProjects\\java-servlet-maven-master\\" + req.getParameter("filename"));
            // - подготавливаем входной поток
            // с помощью которго мы загрузим сначала эту картинку в оперативку,
            // чтобы потом передать ее веб-клиенту,
            // а потом мы возмем параметр filename, который придет с клиента
            // и приклеем это имя в конец шаблона,
            // (в нем лежат ввсе файли на жестком диске сервера)

            // состыковаем вместе два стрима, тот который читает с жесткого диска
            // и тот который выдает результат клиенту
            BufferedInputStream bin = new BufferedInputStream(fin);
            BufferedOutputStream bout = new BufferedOutputStream(out);

            int ch = 0;
            // перебираем байты пока они существуют
            // берем входной поток данных, методом read получаем порции данных
            // возращает -1 если данные закончились
            while ((ch = bin.read()) != -1) {
                // берем этот байт и ево выталкиваем на веб клиента через bout
                // каждый write кидает каждый байтик веб-клиенту
                bout.write(ch);
            }
            // закрываем все стримы
            bin.close();
            fin.close();
            bout.close();
            out.close();
            // HOME WORK
        } else if (req.getParameter("action").equals("getInfo")) {
            // я пока просто скопировал имплементацию предыдущего ифа
            Optional<Model> modelOption = Storage.modelList.stream().filter(m ->
                    m.userName.equals(req.getParameter("username"))
            ).findFirst();
            if (modelOption.isPresent()) {
                Model model = modelOption.get();
                if (model.image != null) {
                    try {
                        resp.getWriter().println(
                                "<!DOCTYPE html>\n" +
                                        "<html lang=\"en\">\n" +
                                        "<head>\n" +
                                        "    <meta charset=\"UTF-8\">\n" +
                                        "    <title>Dream job</title>\n" +
                                        "<style>"+
                                        "section {" +
                                        "position: absolute;" +
                                        "top: 50%;" +
                                        "left: 50%;" +
                                        "margin-right: -50%;" +
                                        "transform: translate(-50%, -50%) }" +
                                        "</style>" +
                                        "</head>\n" +
                                        "<body>\n" +
                                        "<section >" +
                                        "<h1 >" + model.userName + "</h1>\n" +
                                        "<img src='http://127.0.0.1:8888/single-servlet/second-servlet?action=getFile&filename=" + model.image + "' width=\"229\" height=\"274\"/>" +
                                        "<p></p>" +
                                        "<a href='http://127.0.0.1:8888/single-servlet/second-servlet?action=getFileNames'>List of Files</a><br>" +
                                        "<p></p>" +
                                        "<a href='http://127.0.0.1:8888/single-servlet'>Add Files</a><br>" +
                                        "<p></p>" +
                                        "</section>" +
                                        "</body>\n" +
                                        "</html>"
                        );

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else  {
                resp.getWriter().println(
                        "<!DOCTYPE html>\n" +
                                "<html lang=\"en\">\n" +
                                "<head>\n" +
                                "    <meta charset=\"UTF-8\">\n" +
                                "    <title>Dream job</title>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "    <h1>User with name '"+ req.getParameter("username")+"' not found</h1>\n" +

                                "</body>\n" +
                                "</html>"
                );
        }

        //
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
}
