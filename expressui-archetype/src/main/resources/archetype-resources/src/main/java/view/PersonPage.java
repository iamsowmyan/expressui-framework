#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.view;

import ${package}.entity.Person;
import com.expressui.core.view.page.SearchPage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Component
@Scope(SCOPE_SESSION)
@SuppressWarnings({"serial"})
public class PersonPage extends SearchPage<Person> {

    @Resource
    private PersonSearchForm personSearchForm;

    @Resource
    private PersonResults personResults;

    @Override
    public PersonSearchForm getSearchForm() {
        return personSearchForm;
    }

    @Override
    public PersonResults getResults() {
        return personResults;
    }

    @Override
    public String getTypeCaption() {
        return "People";
    }
}

