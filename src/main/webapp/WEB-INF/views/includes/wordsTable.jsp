<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<table class="table table-striped"
       xmlns:spring="http://www.springframework.org/tags"
       xmlns:jsp="http://java.sun.com/JSP/Page"
       xmlns:c="http://java.sun.com/jsp/jstl/core"
       xmlns="http://www.w3.org/1999/xhtml"
       id="table_${param.category}_${param.status}">
    <c:set var="status" value="${controllerStatus}"/>
    <%
        java.util.List<dk.kb.aim.repository.WordStatus> ws = java.util.Arrays.asList(dk.kb.aim.repository.WordStatus.values());
        pageContext.setAttribute("statuses", ws);
        String host = request.getHeader("HOST");
    %>

    <thead>
    <tr>
        <th onclick="sortTable(0, table_${param.category}_${param.status})">id</th>
        <th onclick="sortTable(1, table_${param.category}_${param.status})">English</th>
        <th onclick="sortTable(2, table_${param.category}_${param.status})">Danish</th>
        <c:if test="${status=='ACCEPTED'||status=='REJECTED'}">
            <th colspan="2">Pending</th>
        </c:if>
        <c:if test="${status=='REJECTED'||status=='PENDING'}">
            <th colspan="2">Approve</th>
        </c:if>
        <c:if test="${status=='ACCEPTED'||status=='PENDING'}">
            <th colspan="2">Reject</th>
        </c:if>
        <c:if test="${status=='PENDING'}">
            <th onclick="sortTable(7, table_${param.category}_${param.status})">Count</th>
        </c:if>
        <c:if test="${status!='PENDING'}">
            <th onclick="sortTable(5, table_${param.category}_${param.status})">Count</th>
        </c:if>
        <th>Images</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${words}" var="word">
        <tr>
            <form name="word_form" action="${pageContext.request.contextPath}/update/word" id="word_form_id_${word.id}">
                <td>
                    <span class="spinner-border" role="status" style="display:none">
                        <span class="sr-only">Loading...</span>
                    </span>
                    ${word.id}
                    <input type="hidden" name="id" value="${word.id}"/>
                    <input type="hidden" name="op_category" value=""/>
                </td>
                <td>${word.textEn}<input type="hidden" name="text_en" value="${word.textEn}"/></td>
                <td><input type="text" name="text_da" value="${word.textDa}"/></td>
                <c:if test="${status=='ACCEPTED'||status=='REJECTED'}">
                    <td>
                        <button type="submit"
                                onclick="this.form.op_category.value=this.value"
                                value="PENDING:${word.category}"
                                class="btn btn-warning">
                                Pending
                        </button>
                    </td>
                    <td>
                        <c:if test="${fn:toLowerCase(currentCategory)!='aim'}">
                        <button type="submit"
                                onclick="this.form.op_category.value=this.value"
                                value="PENDING:AIM" class="btn btn-warning">
                                Pending for AIM
                        </button>
                        </c:if>
                    </td>
                </c:if>
                <c:if test="${status=='REJECTED'||status=='PENDING'}">
                    <td>
                        <button type="submit"
                                onclick="this.form.op_category.value=this.value"
                                value="ACCEPTED:${word.category}"
                                class="btn btn-success">
                                Approve
                        </button>
                    </td>
                    <td>
                        <c:if test="${fn:toLowerCase(currentCategory)!='aim'}">
                        <button type="submit"
                                onclick="this.form.op_category.value=this.value"
                                value="ACCEPTED:AIM" class="btn btn-success">
                                Approve for AIM
                        </button>
                        </c:if>
                    </td>
                </c:if>
                <c:if test="${status=='ACCEPTED'||status=='PENDING'}">
                    <td>
                        <button type="submit"
                                onclick="this.form.op_category.value=this.value"
                                value="REJECTED:${word.category}"
                                class="btn btn-danger">
                                Reject
                        </button>
                    </td>
                    <td>
                        <c:if test="${fn:toLowerCase(currentCategory)!='aim'}">
                        <button type="submit"
                                onclick="this.form.op_category.value=this.value"
                                value="REJECTED:AIM"
                                class="btn btn-danger">
                                Reject for AIM
                        </button>
                        </c:if>
                    </td>
                </c:if>
            </form>
            <td>${word.count}</td>
            <td>
                <c:url value="/word_images/${word.id}" var="imgUrl">
                    <c:param name="offset" value="0" />
                    <c:param name="limit" value="12" />
                </c:url>
                <a class="btn btn-info" href="${imgUrl}" role="button">See images</a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<script type="text/javascript">
    var categories = "${categories}".replace('[', '').replace(']', '').split(", ");
    var statuses = "${statuses}".replace('[', '').replace(']', '').split(", ");
</script>

<script>

var frm = $('form[name=word_form]');
var clickedButton = null;

frm.submit(function (e) {
    e.preventDefault();
    var self = $(this);

    console.log($(this).closest('span'));
    $(this).closest('span').show();

    $.ajax({
        type: frm.attr('method'),
        url: frm.attr('action'),
        data: $(this).serialize(),
        success: function (data) {
            var here = self;
            self.closest('tr').fadeOut('fast',
                function(here) {
                    $(here).remove();
                }
            );
            console.log('Submission was successful.');
            //console.log(data)
        },
        error: function (data) {
            spinner.find('span.spinner-border').hide();
            console.log('An error occurred.');
            //console.log(data);
        },
    });
});

/* Taken from example from w3schools.*/
function sortTable(n, table_id) {
    var table, rows, switching, i, x, y, shouldSwitch, ascending, switchcount = 0;
    table = table_id;
    switching = true;
    // Set the sorting direction to ascending:
    ascending = true;

    // TODO: take out values before sorting
    rows = table.rows;
    var values = [];
    for (i = 1; i < rows.length; i++) {
        x = rows[i].getElementsByTagName("TD")[n];
        x_value = x.innerHTML.valueOf();
        if(x_value.replace(/<.*/i, "") && !isNaN(x_value.replace(/<.*/i, ""))) {
            x_value = Number(x_value.replace(/<.*/i, ""));
        }
        values[i] = x_value;
    }

    /* Make a loop that will continue until
    no switching has been done: */
    while (switching) {
        // Start by saying: no switching is done:
        switching = false;
        rows = table.rows;
        /* Loop through all table rows (except the
        first, which contains table headers): */
        for (i = 1; i < (rows.length - 1); i++) {
            // Start by saying there should be no switching:
            shouldSwitch = false;
            /* Get the two elements you want to compare,
            one from current row and one from the next: */
            x_value = values[i];
            y_value = values[i+1];

            /* Check if the two rows should switch place,
            based on the direction, asc or desc: */
            if (ascending) {
                if (x_value > y_value) {
                    // If so, mark as a switch and break the loop:
                    shouldSwitch = true;
                    break;
                }
            } else {
                if (x_value < y_value) {
                    // If so, mark as a switch and break the loop:
                    shouldSwitch = true;
                    break;
                }
            }
        }
        if (shouldSwitch) {
            // Perform switch
            rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
            tempV = values[i];
            values[i] = values[i+1];
            values[i+1] = tempV;

            // Mark switching and increate switching count.
            switching = true;
            switchcount ++;
        } else {
            /* If no switching has been done AND the direction is "asc",
            set the direction to "desc" and run the while loop again. */
            if (switchcount == 0 && ascending) {
                ascending = false;
                switching = true;
            }
        }
    }
}
</script>
