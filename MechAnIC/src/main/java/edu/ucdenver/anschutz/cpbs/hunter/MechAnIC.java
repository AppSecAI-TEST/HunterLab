package edu.ucdenver.anschutz.cpbs.hunter;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;

import java.awt.*;

public class MechAnIC extends AbstractOWLViewComponent {

    private static final Logger log = Logger.getLogger(MechAnIC.class);

    private TextViewer textViewerComponent;

    @Override
    protected void initialiseOWLView() throws Exception {
        setLayout(new BorderLayout());

        textViewerComponent = new TextViewer(getOWLModelManager());
        add(textViewerComponent, BorderLayout.CENTER);

        log.info("MechAnIC initialized");
    }

    @Override
    protected void disposeOWLView() {
        textViewerComponent.dispose();
    }

}
