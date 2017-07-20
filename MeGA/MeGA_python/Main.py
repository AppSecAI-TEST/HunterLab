from Mechanism import Mechanism
from Annotation import FileAnnotator
from Annotation_GUI import AnnotationGUI
import sys
from tkinter import Tk


def test_graphs():
    input_file = 'Inputs/Darden_prerelease2016_fig5_mechanism.txt'
    output_file = 'Outputs/test_graphs.graphml'
    m1 = Mechanism()
    m1.read_from_file(input_file)
    m1.write_to_file(output_file)


def test_annotations():
    input_file = 'Inputs/11532192.txt'
    onto_file = 'Inputs/ontologies_metadata.csv'
    output_file = 'Outputs/test_file_annotation.txt'
    fa1 = FileAnnotator(onto_file=onto_file, use_local=True)
    fa1.begin_annotating_file(input_file, output_file, use_custom=False)


def test_gui():
    root = Tk(className='MeGA')
    root.configure(bg='#2D3142')

    gui = AnnotationGUI(master=root)

    preload_ontology = {
        'File': 'Inputs/ontologies/go-basic.obo',
        'Name': 'GO-Basic',
        'Roots': 'cellular_component,biological_process,molecular_function'.split(',')
    }

    preload_annotation_file = {
        'File': 'Inputs/11532192.txt'
    }

    gui.run(preload_annotation_file=preload_annotation_file, preload_ontology=preload_ontology)


def main(args):
    test_gui()

if __name__ == '__main__':
    main(sys.argv)
