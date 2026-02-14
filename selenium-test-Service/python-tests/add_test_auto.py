from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time
import random

# Setup Chrome driver
driver = webdriver.Chrome()

try:
    # Navigate to the test page
    driver.get("http://localhost:4200/test-selenium")
    wait = WebDriverWait(driver, 10)
    
    # Number of test cases to create
    num_test_cases = 10
    
    for i in range(1, num_test_cases + 1):
        print(f"Creating test case {i}...")
        
        # Add new test case
        add_case_btn = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "button[data-bs-target='#addCase']")))
        add_case_btn.click()
        
        time.sleep(0.5)
        case_name_input = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "#caseName")))
        time.sleep(0.5)
        case_name_input.send_keys(f"Auto Test Case {i}")
        
        time.sleep(0.5)
        submit_case_btn = driver.find_element(By.CSS_SELECTOR, "input[value='Add Case']")
        submit_case_btn.click()
        
        time.sleep(0.5)
        
        # Add new action
        add_action_btn = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "#addActionButton")))
        time.sleep(0.5)
        add_action_btn.click()
        
        action_select = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "#action")))
        time.sleep(0.5)
        action_select.click()
        time.sleep(0.5)
        driver.find_element(By.CSS_SELECTOR, "option[value='1']").click()  # GoToUrl
        
        time.sleep(0.5)
        
        input_field = driver.find_element(By.CSS_SELECTOR, "#input")
        time.sleep(0.5)
        input_field.send_keys(f"https://www.etsmtl.ca/")
        
        submit_action_btn = driver.find_element(By.CSS_SELECTOR, "input[value='Add Action']")
        time.sleep(0.5)
        submit_action_btn.click()
        
        time.sleep(0.5)
        
        
        # Add second action - Click
        add_action_btn = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "#addActionButton")))
        time.sleep(0.5)
        add_action_btn.click()
        
        action_select = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "#action")))
        time.sleep(0.5)
        action_select.click()
        time.sleep(0.5)
        driver.find_element(By.CSS_SELECTOR, "option[value='6']").click()  #Click 
        
        time.sleep(0.3)
        
        # Fill in target output for Click action
        target_output_field = driver.find_element(By.CSS_SELECTOR, "#object")
        time.sleep(1)
        target_output_field.send_keys("button.o-input")
        
        submit_action_btn = driver.find_element(By.CSS_SELECTOR, "input[value='Add Action']")
        time.sleep(0.5)
        submit_action_btn.click()
        
        # Add third action - FillField
        add_action_btn = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "#addActionButton")))
        time.sleep(0.5)
        add_action_btn.click()
        
        action_select = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "#action")))
        time.sleep(0.5)
        action_select.click()
        time.sleep(0.5)
        driver.find_element(By.CSS_SELECTOR, "option[value='2']").click()  #Click 
        
        time.sleep(0.3)
        
        # Fill in target output for FillField action
        target_output_field = driver.find_element(By.CSS_SELECTOR, "#object")
        time.sleep(1)
        target_output_field.send_keys("input.o-input")
        
        target_output_field = driver.find_element(By.CSS_SELECTOR, "#input")
        time.sleep(1)
        target_output_field.send_keys("Test Input")
        
        
        
        submit_action_btn = driver.find_element(By.CSS_SELECTOR, "input[value='Add Action']")
        time.sleep(0.5)
        submit_action_btn.click()
        
        print(f"✓ Test case {i} with 2 actions added successfully!")
        time.sleep(0.5)
    
    print(f"\n✓ All {num_test_cases} test cases created successfully!")
    print("You can now test parallel execution of these test cases.")
    time.sleep(100000)
    
finally:
    driver.quit()
