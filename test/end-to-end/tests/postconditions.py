from selenium.webdriver.common.by import By
from selenium.common.exceptions import NoSuchElementException
from tests import test_data


def remove_application(driver):
    try:
        driver.get('https://github.com/settings/applications')
        driver.find_element(By.CSS_SELECTOR, '.BtnGroup-item').click()
        driver.find_element(By.CSS_SELECTOR, '.facebox-popup .btn-danger').click()
    except NoSuchElementException:
        pass


def remove_installation(driver):
    try:
        driver.get(test_data.config['ORG']['gh_org_profile'] + 'settings/installations')
        driver.find_element(By.CSS_SELECTOR, '.iconbutton').click()
        driver.find_element(By.XPATH, "//a[@class='btn btn-danger']").click()
        driver.find_element(By.CSS_SELECTOR, '.facebox-popup .btn-danger').click()
    except NoSuchElementException:
        pass
