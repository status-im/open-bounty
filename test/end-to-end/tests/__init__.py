import configparser, time, datetime, os


class TestData(object):

    def __init__(self):
        self.test_name = None
        self.config = configparser.ConfigParser()

        # put config.ini to /test/end-to-end/tests folder (same directory where config_example.ini is placed
        self.tests_path = os.path.abspath(os.path.dirname(__file__))
        self.config.read(os.path.join(self.tests_path, 'config.ini'))

        # create unique identificator for PRs, issues ect
        ts = time.time()
        st = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
        self.date_time = st

        # self.issue['title'] is set in GithubPage::create_new_bounty
        # self.issue['id'] is set in GithubPage::create_new_bounty


test_data = TestData()
