import pytest, sys
from selenium import webdriver
from selenium.common.exceptions import WebDriverException
from tests.postconditions import remove_application, remove_installation
from os import environ, path
from tests import test_data
from pages.thirdparty.github import GithubPage
from pages.openbounty.landing import LandingPage

class BaseTestCase:


    def print_sauce_lab_info(self, driver):
        sys.stdout = sys.stderr
        print("SauceOnDemandSessionID=%s job-name=%s" % (driver.session_id,
                                                         pytest.config.getoption('build')))
    def get_remote_caps(self):
        sauce_lab_cap = dict()
        sauce_lab_cap['name'] = test_data.test_name
        sauce_lab_cap['build'] = pytest.config.getoption('build')
        sauce_lab_cap['idleTimeout'] = 900
        sauce_lab_cap['commandTimeout'] = 500
        sauce_lab_cap['platform'] = "MAC"
        sauce_lab_cap['browserName'] = 'Chrome'
        sauce_lab_cap['screenResolution'] = '2048x1536'
        sauce_lab_cap['captureHtml'] = False
        return sauce_lab_cap

    def verify_no_errors(self):
        if self.errors:
            msg = ''
            for error in self.errors:
                msg += (error + '\n')
            pytest.fail(msg, pytrace=False)

    @classmethod
    def setup_class(cls):
        cls.errors = []
        cls.environment =  pytest.config.getoption('env')

###################################################################################################################
######### Drivers setup
###################################################################################################################

        #
        # Dev Chrome options
        #
        cls.capabilities_dev = webdriver.ChromeOptions()
        cls.capabilities_dev.add_argument('--start-fullscreen')

        #
        # Org Chrome options
        #
        cls.capabilities_org = webdriver.ChromeOptions()
        # doesn't work on sauce env
        # cls.capabilities_org.add_extension(path.abspath(test_data.config['Paths']['tests_absolute'] + 'resources/metamask3_12_0.crx'))

        #
        # SauceLab capabilities
        #testcommit to check GPG signature 2
        cls.executor_sauce_lab = 'http://%s:%s@ondemand.saucelabs.com:80/wd/hub' % (
        environ.get('SAUCE_USERNAME'), environ.get('SAUCE_ACCESS_KEY'))
        drivers = []

        if cls.environment == 'local':
            for caps in cls.capabilities_dev, cls.capabilities_org:
                driver = webdriver.Chrome(chrome_options=caps)
                drivers.append(driver)

        if cls.environment == 'sauce':
            for caps in cls.capabilities_dev, cls.capabilities_org:
                remote = cls.get_remote_caps(cls)
                new_caps = caps.to_capabilities()
                new_caps.update(remote)
                driver = webdriver.Remote(cls.executor_sauce_lab,
                                          desired_capabilities=new_caps)
                drivers.append(driver)

            for driver in drivers:
                cls.print_sauce_lab_info(cls, driver)

        cls.driver_dev = drivers[0]
        cls.driver_org = drivers[1]


        for driver in drivers:
             driver.implicitly_wait(10)

###################################################################################################################
######### Actions for each driver before class
###################################################################################################################

        ######ORG
        landing = LandingPage(cls.driver_org)
        landing.get_landing_page()

         # Sign Up to SOB
        cls.github_org = landing.login_button.click()
        cls.github_org.sign_in(test_data.config['ORG']['gh_login'],
                                test_data.config['ORG']['gh_password'])
        assert cls.github_org.permission_type.text == 'Personal user data'
        bounties_page = cls.github_org.authorize_sob.click()

        # SOB Plugin installation and navigate to "Open bounties"
        cls.github_org.install_sob_plugin()
        assert bounties_page.bounties_header.text == 'Bounties'
        assert bounties_page.top_hunters_header.text == 'Top 5 hunters'

        ######DEV
        cls.github_dev = GithubPage(cls.driver_dev)
        # Sign In to GH as Developer
        cls.github_dev.get_login_page()
        cls.github_dev.sign_in(test_data.config['DEV']['gh_login'],
                        test_data.config['DEV']['gh_password'])

         # Fork repo as Developer from Organization
        cls.github_dev.fork_repo(test_data.config['ORG']['gh_repo'])

         # Cloning repo to local git as Developer and set upstream to Organization (via HTTPS)
        cls.github_dev.clone_repo(test_data.config['ORG']['gh_repo'],
                           test_data.config['DEV']['gh_username'],
                           test_data.config['ORG']['gh_repo_name'],
                           'git_repo')
        cls.verify_no_errors(cls)




    @classmethod
    def teardown_class(cls):

        ######ORG

        # SOB Plugin remove installation
        remove_application(cls.driver_org)
        remove_installation(cls.driver_org)

        ######DEV
        cls.github_dev.clean_repo_local_folder()
        cls.github_dev.delete_fork()

        try:
            cls.driver_dev.quit()
            cls.driver_org.quit()
        except WebDriverException:
            pass



