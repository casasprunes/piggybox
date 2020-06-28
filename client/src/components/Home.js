import React, { useState } from "react";
import { Container, makeStyles } from "@material-ui/core";
import ArcProgress from "react-arc-progress";
import Button from "./shared/Button";
import ButtonSecondary from "./shared/ButtonSecondary";
import AddFunds from "./AddFunds";

const useStyles = makeStyles((theme) => ({
  gauge: {
    display: "flex",
    justifyContent: "center",
    marginTop: 90,
  },
  piggy: {
    width: 150,
    height: 150,
    position: "absolute",
    top: 200,
  },
  money: {
    position: "absolute",
    top: 400,
    fontSize: 50,
    fontFamily: "raleway",
  },
  buttons: {
    position: "fixed",
    top: "430px",
    display: "flex",
    justifyContent: "center",
  },
}));

const { progress, text } = {
  progress: 0.25,
  text: "40.97",
};

const Home = (props) => {
  const classes = useStyles();
  const [open, setOpen] = useState(false);

  return (
    <div>
      <div className={classes.gauge}>
        <img src="./piggy.png" alt="Piggy Bank" className={classes.piggy} />
        <ArcProgress
          size={350}
          thickness={10}
          progress={progress}
          text={text}
          fillColor={{ gradient: ["#ffe25b", "#ffcf3a"] }}
          textStyle={{
            y: 220,
            size: "40px",
            color: "#000",
            font: "Raleway",
          }}
          customText={[
            {
              text: "USD",
              size: "20px",
              color: "#000",
              x: 175,
              y: 255,
              font: "Raleway",
            },
          ]}
          arcStart={-190}
          arcEnd={10}
          observer={(current) => {
            const { percentage, currentText } = current;
            console.log("observer:", percentage, currentText);
          }}
          animationEnd={({ progress, text }) => {
            console.log("animationEnd", progress, text);
          }}
        />
      </div>
      <Container className={classes.buttons}>
        <Button style={{ margin: "5px" }} onClick={() => setOpen(true)}>
          Add
        </Button>
        <ButtonSecondary style={{ margin: "5px" }}>Withdraw</ButtonSecondary>
      </Container>
      <AddFunds onClose={() => setOpen(false)} open={open} />
    </div>
  );
};

export default Home;
