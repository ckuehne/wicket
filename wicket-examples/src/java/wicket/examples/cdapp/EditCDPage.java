/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.cdapp;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.PageParameters;
import wicket.WicketRuntimeException;
import wicket.contrib.data.model.PersistentObjectModel;
import wicket.contrib.data.model.hibernate.HibernateObjectModel;
import wicket.contrib.data.util.hibernate.HibernateHelper;
import wicket.contrib.data.util.hibernate.HibernateHelperSessionDelegate;
import wicket.contrib.markup.html.form.DataTextField;
import wicket.examples.cdapp.model.CD;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.markup.html.form.validation.IntegerValidator;
import wicket.markup.html.form.validation.LengthValidator;
import wicket.markup.html.form.validation.RequiredValidator;
import wicket.markup.html.link.OnClickLink;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.DetachableModel;
import wicket.model.IModel;


/**
 * Page for editing CD's.
 * 
 * @author Eelco Hillenius
 */
public final class EditCDPage extends SimpleBorderedPage
{
	/** Logger. */
	private static Log log = LogFactory.getLog(SearchCDPage.class);

	/** model for one cd. */
	private PersistentObjectModel cdModel;

	/**
	 * Constructor for new CD.
	 * 
	 * @param parameters Page parameters
	 */
	public EditCDPage(final PageParameters parameters)
	{
		Long id = null;
		String idFromRequest = parameters.getString("id");
		if (idFromRequest != null)
		{
			id = Long.valueOf(idFromRequest);
		}
		cdModel = new HibernateObjectModel(id, CD.class, new HibernateHelperSessionDelegate());
		// add a label with expression title. What happens is that a
		// PropertyModel is constructed with our instance of IModel
		// (DetachableCDModel) and
		// property expression 'title'. At execution time, our DetachableCDModel
		// will
		// load the CD as the actual model. That CD will be used for the
		// expression,
		// thus property 'title' of the current CD will be used for the label.
		add(new Label("cdTitle", new TitleModel(cdModel)));
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new DetailForm("detailForm", feedback, cdModel));
	}

	/**
	 * @see wicket.Component#initModel()
	 */
	protected IModel initModel()
	{
		return cdModel;
	}

	/**
	 * form for detail editing.
	 */
	private final class DetailForm extends Form
	{
		/**
		 * Construct.
		 * 
		 * @param name component name
		 * @param validationErrorHandler error handler
		 * @param cdModel the model
		 */
		public DetailForm(String name, IValidationFeedback validationErrorHandler,
				PersistentObjectModel cdModel)
		{
			super(name, cdModel, validationErrorHandler);
			DataTextField titleField = new DataTextField("title", cdModel, "title");
			titleField.add(RequiredValidator.getInstance());
			titleField.add(LengthValidator.max(50));
			add(titleField);
			DataTextField performersField = new DataTextField("performers", cdModel, "performers");
			performersField.add(RequiredValidator.getInstance());
			performersField.add(LengthValidator.max(50));
			add(performersField);
			DataTextField labelField = new DataTextField("label", cdModel, "label");
			labelField.add(LengthValidator.max(50));
			add(labelField);
			DataTextField yearField = new DataTextField("year", cdModel, "year");
			yearField.add(RequiredValidator.getInstance());
			yearField.add(IntegerValidator.POSITIVE_INT);
			add(yearField);
			add(new OnClickLink("cancelButton")
			{
				public void onClick()
				{
					//getRequestCycle().setPage(getSearchPage());
				}
			});
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		public void onSubmit()
		{
			CD cd = (CD)getModelObject();
			// note that, as we used the Ognl property model, the fields are
			// allready updated
			Session session = null;
			Transaction tx = null;
			try
			{
				session = HibernateHelper.getSession();
				tx = session.beginTransaction();
				session.save(cd);
				tx.commit();
				info("cd saved");
				//getRequestCycle().setPage(getSearchPage());
			}
			catch (HibernateException e)
			{
				try
				{
					tx.rollback();
				}
				catch (HibernateException ex)
				{
					ex.printStackTrace();
				}
				throw new WicketRuntimeException(e);
			}
		}
	}

	/**
	 * Special model for the title header. It returns the CD title if there's a
	 * loaded object (when the id != null) or it returns a special string in case
	 * there is no loaded object (if id == null).
	 */
	private static class TitleModel extends DetachableModel
	{
		/** decorated model; provides the current id. */
		private final PersistentObjectModel cdModel;

		/**
		 * Construct.
		 * 
		 * @param cdModel the model to decorate
		 */
		public TitleModel(PersistentObjectModel cdModel)
		{
			super(null);
			this.cdModel = cdModel;
		}

		/**
		 * @see wicket.model.IModel#getObject()
		 */
		public Object getObject()
		{
			if (cdModel.getId() != null)
			{
				CD cd = (CD)cdModel.getObject();
				return cd.getTitle();
			}
			else
			{
				return "<NEW CD>";
			}
		}

		/**
		 * @see wicket.model.IModel#setObject(java.lang.Object)
		 */
		public void setObject(Object object)
		{
			cdModel.setObject(object);
		}

		/**
		 * @see wicket.model.DetachableModel#onDetach()
		 */
		public void onDetach()
		{
			cdModel.detach();
		}

		/**
		 * @see wicket.model.DetachableModel#onAttach()
		 */
		public void onAttach()
		{
			cdModel.attach();
		}
	}
}