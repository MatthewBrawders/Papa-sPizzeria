CREATE OR REPLACE FUNCTION auto_increment_orderID()
RETURNS TRIGGER AS $$
BEGIN
    -- Get the current maximum orderID and increment it by 1
    NEW.orderID := COALESCE((SELECT MAX(orderID) FROM FoodOrder), 10000) + 1;

    -- Return the modified NEW row
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Attach the trigger to the FoodOrder table
CREATE TRIGGER set_orderID
BEFORE INSERT ON FoodOrder
FOR EACH ROW
EXECUTE FUNCTION auto_increment_orderID();