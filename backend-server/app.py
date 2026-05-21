import os
import json
import stripe
from dotenv import load_dotenv
from google import genai
from google.api_core import exceptions
from flask import Flask, request, jsonify
from flask_cors import CORS

load_dotenv("gemini.env")

# Initialize with stable v1
client = genai.Client(
    api_key="key",
    http_options={'api_version': 'v1'}
)

# Initialize Stripe
stripe.api_key = "key"

app = Flask(__name__)
CORS(app)

@app.route('/explainResults', methods=['POST'])
def explain_results():
    data = request.json
    score = data.get('score', 0)
    total = data.get('total', 5)
    topic = data.get('topic', 'IT')

    prompt = (
        f"The user scored {score} out of {total} on a quiz about {topic}. "
        "Provide a short, encouraging summary (max 2 sentences) "
        "and suggest one area to focus on."
    )

    try:
        response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=prompt
        )
        return jsonify({"explanation": response.text.strip()})
    except Exception as e:
        print(f"Explanation Error: {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    topic = request.args.get('topic', 'General IT')
    prompt = (
        f"Generate a 5-question multiple choice quiz about {topic}. "
        "Return as a raw JSON object with a 'quiz' array. "
        "Fields: 'questionText', 'options' (4), 'correctAnswerIndex' (0-3), 'topic'."
    )

    try:
        response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=prompt
        )
        return jsonify(json.loads(response.text.strip().strip('`json').strip('`')))
    except exceptions.ResourceExhausted as e:
        print(f"?? QUOTA HIT: {e}")
        return jsonify({
            "error": "Rate limit exceeded. Please wait a moment.",
            "retry_after": 60,
            "quiz": []
        }), 429
    except Exception as e:
        print(f" ERROR: {e}")
        return jsonify({"error": str(e), "quiz": []}), 500

@app.route('/createPaymentIntent', methods=['POST'])
def create_payment_intent():
    print("🔍 Payment endpoint hit!")
    data = request.json
    tier = data.get('tier', 'Intermediate')
    username = data.get('username', 'anonymous')

    tier_amounts = {
        'Starter': 299,
        'Intermediate': 499,
        'Advanced': 799
    }
    amount = tier_amounts.get(tier, 499)

    try:
        intent = stripe.PaymentIntent.create(
            amount=amount,
            currency='usd',
            metadata={'tier': tier, 'username': username}
        )
        print(f"✅ PaymentIntent created: {intent.id}")
        return jsonify({
            'clientSecret': intent.client_secret,
            'paymentIntentId': intent.id,
            'tier': tier,
            'amount': amount / 100.0
        })
    except Exception as e:
        print(f"❌ PaymentIntent Error: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080, debug=True)