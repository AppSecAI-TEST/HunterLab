from Mechanism import Mechanism
from Annotation import FileAnnotator
import sys


def test_graphs():
    input_file = 'Inputs/Darden_prerelease2016_fig5_mechanism.txt'
    output_file = 'Outputs/test_graphs.graphml'
    m1 = Mechanism()
    m1.read_from_file(input_file)
    m1.write_to_file(output_file)


def test_annotations():
    input_file = 'Inputs/11532192.txt'
    output_file = 'Outputs/test_file_annotation.txt'
    fa1 = FileAnnotator()
    fa1.begin_annotating_file(input_file, output_file, use_custom=False, use_local=True)


def main(args):
    test_annotations()

if __name__ == '__main__':
    main(sys.argv)
